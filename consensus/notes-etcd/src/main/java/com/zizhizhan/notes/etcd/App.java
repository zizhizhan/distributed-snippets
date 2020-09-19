package com.zizhizhan.notes.etcd;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchResponse;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
public class App {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        String prefix = "/etcd/kvstore/";
        ByteSequence prefixBS = ByteSequence.from(prefix, StandardCharsets.UTF_8);

        List<String> serverList = Arrays.asList("http://127.0.0.1:2379", "http://127.0.0.1:2380");
        Client client = Client.builder().endpoints(serverList
                .stream()
                .map(URI::create)
                .collect(Collectors.toList())).build();

        WatchOption watchOption = WatchOption.newBuilder().withPrefix(prefixBS).build();
        client.getWatchClient().watch(prefixBS, watchOption, new Watch.Listener() {
            @Override
            public void onNext(WatchResponse response) {
                List<WatchEvent> events = response.getEvents();
                if (events != null && !events.isEmpty()) {
                    log.info("Received events {} for {}.", events, response);
                    events.forEach(e -> {
                        KeyValue kv = e.getKeyValue();
                        switch (e.getEventType()) {
                            case PUT:
                                log.info("add config {}: {}", kv.getKey().toString(StandardCharsets.UTF_8), kv.getValue().toString(StandardCharsets.UTF_8));
                                break;
                            case DELETE:
                                log.info("delete config {}: {}", kv.getKey().toString(StandardCharsets.UTF_8), kv.getValue().toString(StandardCharsets.UTF_8));
                                break;
                            default:
                                log.info("unrecognized config {}: {}", kv.getKey().toString(StandardCharsets.UTF_8), kv.getValue().toString(StandardCharsets.UTF_8));
                        }
                    });
                }
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("etcd instance error", throwable);
            }

            @Override
            public void onCompleted() {
                log.error("etcd instance complete");
            }
        });

        long ttl = 20;
        LeaseGrantResponse response = client.getLeaseClient().grant(ttl).get();
        client.getLeaseClient().keepAlive(response.getID(), new StreamObserver<LeaseKeepAliveResponse>() {
            @Override
            public void onNext(LeaseKeepAliveResponse leaseKeepAliveResponse) {
                log.info("keepAlive invoke");
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("keepAlive onError invoke", throwable);
            }

            @Override
            public void onCompleted() {
                log.info("keepAlive onCompleted invoke");
            }
        });

        PutOption putOption = PutOption.newBuilder().withLeaseId(response.getID()).build();
        client.getKVClient().put(ByteSequence.from("/etcd/kvstore/key01", StandardCharsets.UTF_8),
                ByteSequence.from("foo", StandardCharsets.UTF_8), putOption);
        client.getKVClient().put(ByteSequence.from("/etcd/kvstore/key02", StandardCharsets.UTF_8),
                ByteSequence.from("bar", StandardCharsets.UTF_8), putOption);
        client.getKVClient().put(ByteSequence.from("/etcd/kvstore/key03", StandardCharsets.UTF_8),
                ByteSequence.from("baz", StandardCharsets.UTF_8), putOption);

        Thread.sleep(3000);

        GetOption getOption = GetOption.newBuilder().withPrefix(ByteSequence.from("/etcd/kvstore/", StandardCharsets.UTF_8)).build();
        GetResponse getResponse = client.getKVClient().get(ByteSequence.from("/etcd/kvstore/",
                StandardCharsets.UTF_8), getOption).get();

        long aliveCount = getResponse.getCount();
        log.info("available count is {} ", aliveCount);
        if (aliveCount > 0) {
            List<KeyValue> kvs = getResponse.getKvs();
            kvs.stream().map(kv -> String.format("%s: %s", kv.getKey().toString(StandardCharsets.UTF_8),
                    kv.getValue().toString(StandardCharsets.UTF_8))).forEach(System.out::println);
        }
    }

}
