package com.zizhizhan.kafka.streams;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic kafka-analyse-topic
 *
 */
public class KafkaStreamsAnalyse {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamsAnalyse.class);

    public static void main(final String[] args) {
        String appId = "kafka-streams-analyse";
        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, String> source = builder.stream("streams-plaintext-input");
        final KTable<String, Long> counts = source
                .filter(predicate("source"))
                .flatMapValues(value -> Arrays.asList(value.toLowerCase(Locale.getDefault()).split(" ")))
                .filter(predicate("flatMapValues"))
                .groupBy((key, value) -> value)
                .aggregate(() -> 0L, (aggKey, value, aggregate) -> {
                    LOGGER.info("group is ({}, {}, {}).", aggKey, value, aggregate);
                    return aggregate + 1;
                }, Materialized.with(Serdes.String(), Serdes.Long()));

        counts.filter(predicate("counts")).toStream().foreach((k, v) -> {
            LOGGER.info("Get {} with count {}.", k, v);
        });

        startKafkaStreams(appId, "localhost:9092", builder.build());
    }

    private static Properties newKafkaConfig(String applicationId, String bootstrapServers) {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    private static void startKafkaStreams(String appId, String bootstrapServers, Topology topology) {
        final KafkaStreams streams = new KafkaStreams(topology, newKafkaConfig(appId, bootstrapServers));
        final CountDownLatch latch = new CountDownLatch(1);

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread(appId) {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (final Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }

    private static <K, V> Predicate<K, V> predicate(String label) {
        return (k, v) -> {
            LOGGER.debug("{} is ({}: [{}])", label, k, v);
            return true;
        };
    }
}
