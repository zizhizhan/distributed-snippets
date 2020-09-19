package com.zizhizhan.kafka;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.zizhizhan.utils.IoUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import javax.annotation.Nonnull;
import java.io.File;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * brew install kafka
 * zookeeper-server-start /usr/local/etc/kafka/zookeeper.properties & kafka-server-start /usr/local/etc/kafka/server.properties
 * telnet localhost:9092
 */
@Slf4j
public class KafkaWithKerberos {
    private static final String TOPIC = "test-topic";

    private static final String JAAS_TEMPLATE = "KafkaClient '{'\n" +
            "\tcom.sun.security.auth.module.Krb5LoginModule required\n" +
            "\tuseKeyTab=true\n" +
            "\tstoreKey=true\n" +
            "\tkeyTab=\"{0}\"\n" +
            "\tprincipal=\"{1}\";\n" +
            "'}';";

    private static final String BOOTSTRAP_SERVERS = "master001:9092";

    private static final LoadingCache<String, KafkaProducer<String, String>> producerCache = CacheBuilder.newBuilder()
            .maximumSize(512)
            .expireAfterWrite(300, TimeUnit.SECONDS)
            .removalListener(n -> {
                log.info("[RemovalNotification] {} with {} by {}.", n.getKey(), n.getValue(), n.getCause());
            })
            .build(new CacheLoader<String, KafkaProducer<String, String>>() {
                public KafkaProducer<String, String> load(@Nonnull String key) {
                    log.info("Not found {}, create it!", key);
                    return createProducer();
                }
            });

    static {
        configureKerberosJAAS();
    }

    public static void main(String[] args) throws InterruptedException {
        new Thread(() -> {
            runConsumer("test-kafka-consumer");
        }).start();

        for (int i = 0; i < 100000; i++) {
            runProducer(i);
            Thread.sleep(10000);
        }
    }

    private static void runProducer(final int epoch) {
        final Producer<String, String> producer = getKafkaProducer(TOPIC);
        long time = System.currentTimeMillis();

        for (int i = 0; i < 10; i++) {
            sendHelloMessage(producer, epoch, i);
            log.debug("send {}-{}.", epoch, i);
        }
        long elapsedTime = System.currentTimeMillis() - time;
        log.info("Epoch {} finished, cost {}ms.", epoch, elapsedTime);
    }

    private static void runConsumer(String consumerGroup) {
        final Consumer<String, String> consumer = createConsumer(consumerGroup);
        final int giveUp = 100;
        int noRecordsCount = 0;

        while (true) {
            final ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(10000));
            if (consumerRecords.count() == 0) {
                noRecordsCount++;
                if (noRecordsCount > giveUp) {
                    break;
                } else {
                    continue;
                }
            }

            StringBuilder sb = new StringBuilder("Consumer Record:{\n");
            consumerRecords.forEach(record -> {
                sb.append("\t(").append(record.key()).append(", ").append(record.value()).append(", ")
                        .append(record.partition()).append(", ").append(record.offset()).append(")\n");
            });
            sb.append("}\n");
            System.out.println(sb);
            consumer.commitAsync();
        }
        consumer.close();
        System.out.println("DONE");
    }

    public static Producer<String, String> getKafkaProducer(String topic) {
        try {
            KafkaProducer<String, String> producer = producerCache.get(topic);
            try {
                producer.send(new ProducerRecord<>("heartbeat-message", "check-server-available")).get();
            } catch (InterruptedException e) {
                log.warn("Unexpected error while sending heartbeat message.");
            } catch (ExecutionException e){
                if (e.getCause() instanceof TimeoutException) {
                    log.error("{} is timeout should create a new one.", producer);
                    producerCache.invalidate(topic);
                    producer = createProducer();
                    producerCache.put(topic, producer);
                }
            }
            return producer;
        } catch (ExecutionException e) {
            log.error("Can't create KafkaProducer.");
            throw new IllegalStateException("Can't create KafkaProducer", e);
        }
    }

    private static KafkaProducer<String, String> createProducer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put("security.protocol", "SASL_PLAINTEXT");
        props.put("sasl.mechanism", "GSSAPI");
        props.put("sasl.kerberos.service.name", "kafka");
        props.put("acks", "-1");
        props.put("retries", 3);
        props.put("batch.size", 323840);
        props.put("linger.ms", 10);
        props.put("buffer.memory", 33554432);
        props.put("max.block.ms", 3000);
        props.put("connections.max.idle.ms", 10000); // 540000
        return new KafkaProducer<>(props);
    }

    private static Consumer<String, String> createConsumer(String consumerGroup) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put("security.protocol", "SASL_PLAINTEXT");
        props.put("sasl.mechanism", "GSSAPI");
        props.put("sasl.kerberos.service.name", "kafka");
        final Consumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(TOPIC));
        return consumer;
    }

    private static void sendHelloMessage(Producer<String, String> producer, int epoll, int index) {
        final ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, "Epoch-" + epoll,
                "Hello: " + index);
        producer.send(record);
    }

    private static void configureKerberosJAAS() {
        File krb5Conf = IoUtils.copyResourceAsTempFile("krb5.conf", "krb5-", ".conf");
        System.setProperty("java.security.krb5.conf", krb5Conf.getPath());
        File keytab = IoUtils.copyResourceAsTempFile("deploy.keytab", "deploy-", ".keytab");
        String content = MessageFormat.format(JAAS_TEMPLATE, keytab.getPath(), "deploy@ZIZHIZHAN.COM");
        File jaasConf = IoUtils.saveContentAsTempFile(content, "jaas-", ".conf");
        System.setProperty("java.security.auth.login.config", jaasConf.getPath());
        log.info("krb5Config file is {}.", krb5Conf.getPath());
        log.info("keytab file is {}.", keytab.getPath());
        log.info("JAASConfig file is {}.", jaasConf.getPath());
        // System.setProperty("sun.security.krb5.debug", "true");
    }

}
