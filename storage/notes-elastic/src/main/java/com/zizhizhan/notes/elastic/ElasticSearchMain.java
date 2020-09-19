package com.zizhizhan.notes.elastic;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ElasticSearchMain {

    public static void main(String[] args) throws IOException {
        RestHighLevelClient client = ElasticSearchUtils.newRestClient("elastic", null, "127.0.0.1", 9200);
//
//        Long startTimeStamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24;
//        Long endTimeStamp = System.currentTimeMillis();

        SearchRequest searchRequest = new SearchRequest();
        // RangeQueryBuilder timeQueryBuilder = QueryBuilders.rangeQuery("@timestamp").from(startTimeStamp).to(endTimeStamp);
        // BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().must(timeQueryBuilder);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.from(0).size(10000);
        searchSourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC));
        searchSourceBuilder.sort("@timestamp", SortOrder.DESC);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        // searchRequest.indices("logstash-*");

        String keyword = "";
        if (StringUtils.isEmpty(keyword)) {
            searchRequest.source(searchSourceBuilder);
            SearchResponse result = client.search(searchRequest, RequestOptions.DEFAULT);
            System.out.println(result);
        } else {
            HighlightBuilder highlightBuilder = new HighlightBuilder().field("log_time").field("content");
            highlightBuilder.preTags("<span style=\"background:yellow\">");
            highlightBuilder.postTags("</span>");
            highlightBuilder.fragmentSize(10000);
            searchRequest.source(searchSourceBuilder);
            SearchResponse result = client.search(searchRequest, RequestOptions.DEFAULT);
            System.out.println(getHighLightResponse(result));
        }
    }


    private static Object getHighLightResponse(SearchResponse result) {
        Map<String, Object> map = Maps.newHashMap();
        List<Map> list = Lists.newArrayList();

        SearchHits hits = result.getHits();
        TotalHits totalHits = hits.getTotalHits();

        for (SearchHit hit : hits) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField logTimeField = highlightFields.get("log_time");
            HighlightField contentField = highlightFields.get("content");

            if (logTimeField != null) {
                //获得高亮标签
                Text[] fragments = logTimeField.getFragments();
                String nameTmp = "";
                for (Text text : fragments) {
                    nameTmp += text;
                }
                sourceAsMap.put("log_time", nameTmp);
            }

            if (contentField != null) {
                Text[] fragments = contentField.getFragments();
                String nameTmp = "";
                for (Text text : fragments) {
                    nameTmp += text;
                }
                sourceAsMap.put("content", nameTmp);
            }

            list.add(sourceAsMap);

        }
        map.put("total", totalHits);
        map.put("logList", list);
        return map;
    }
}
