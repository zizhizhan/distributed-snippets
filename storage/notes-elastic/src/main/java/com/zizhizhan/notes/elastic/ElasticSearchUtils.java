package com.zizhizhan.notes.elastic;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.*;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Slf4j
public class ElasticSearchUtils {

    private static BulkProcessor bulkProcessor;

    public static void initEs(String user, String pwd, String nodes, int port) {
        if (bulkProcessor == null) {
            synchronized (ElasticSearchUtils.class) {
                if (bulkProcessor == null) {
                    RestHighLevelClient transportClient = newRestClient(user, pwd, nodes, port);
                    bulkProcessor = buildProcessor(transportClient);
                }
            }
        }
    }

    public static RestHighLevelClient newRestClient(String user, String pwd, String nodes, int port) {
        String[] split = nodes.split(";");
        List<String> hostList = new ArrayList<>();
        for (String s : split) {
            if (StringUtils.isEmpty(s)) {
                continue;
            }
            hostList.add(s);
        }
        return new RestHighLevelClient(buildClient(user, pwd, hostList, port));
    }

    private static RestClientBuilder buildClient(String user, String pwd, List<String> nodes, int port) {
        /*RestClientBuilder 在构建 RestClient 实例时可以设置以下的可选配置参数new */
        List<HttpHost> collect = nodes.stream().filter(s -> !StringUtils.isEmpty(s)).map(s -> new HttpHost(s, port))
                .collect(Collectors.toList());

        RestClientBuilder restClientBuilder = RestClient.builder(collect.toArray(new HttpHost[collect.size()]));

        /*2.设置在同一请求进行多次尝试时应该遵守的超时时间。默认值为30秒，与默认`socket`超时相同。
            如果自定义设置了`socket`超时，则应该相应地调整最大重试超时。*/

        // restClientBuilder.setMaxRetryTimeoutMillis(10000);

        /*3.设置每次节点发生故障时收到通知的侦听器。内部嗅探到故障时被启用。*/
        restClientBuilder.setFailureListener(new RestClient.FailureListener() {
            @Override
            public void onFailure(Node host) {
                log.error("Host {} is failure.", host);
            }
        });
        /*4.设置修改默认请求配置的回调（例如：请求超时，认证，或者其他
         设置）。
         */
        restClientBuilder.setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder.setSocketTimeout(10000));

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, pwd));

        restClientBuilder.setHttpClientConfigCallback(httpClientBuilder -> {
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            //线程设置
            httpClientBuilder.setDefaultIOReactorConfig(IOReactorConfig.custom().setIoThreadCount(100).build());
            return httpClientBuilder;
        });

        return restClientBuilder;
    }

    public static void addDocument(String index, Map<String, Object> vMap) {
        bulkProcessor.add(new IndexRequest(index, "_doc").source(JSON.toJSONString(vMap), XContentType.JSON));
    }

    /**
     * build processor
     *
     * @param highLevelClient
     * @return
     */
    private static BulkProcessor buildProcessor(RestHighLevelClient highLevelClient) {
        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {

            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                log.info("insert bulk {} success", request.numberOfActions());

            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                log.error("insert bulk {} fail", request.numberOfActions());
            }
        };

        BiConsumer<BulkRequest, ActionListener<BulkResponse>> bulkConsumer =
                (request, bulkListener) -> highLevelClient.bulkAsync(request, RequestOptions.DEFAULT, bulkListener);
        return BulkProcessor.builder(bulkConsumer, listener).setBulkActions(500).setBulkSize(new ByteSizeValue(1, ByteSizeUnit.MB)).setFlushInterval(TimeValue.timeValueSeconds(5)).setConcurrentRequests(1).setBackoffPolicy(
                BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3)).build();
    }




}
