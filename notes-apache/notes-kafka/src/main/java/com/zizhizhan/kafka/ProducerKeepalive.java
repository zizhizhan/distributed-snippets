package com.zizhizhan.kafka;

import com.zizhizhan.utils.IoUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.File;
import java.text.MessageFormat;
import java.util.Properties;

@Slf4j
public class ProducerKeepalive {

    private static final String BOOTSTRAP_SERVERS = "master001:9092";
    private static final String TOPIC = "test-topic";

    private static final String JAAS_TEMPLATE = "KafkaClient '{'\n" +
            "\tcom.sun.security.auth.module.Krb5LoginModule required\n" +
            "\tuseKeyTab=true\n" +
            "\tstoreKey=true\n" +
            "\tkeyTab=\"{0}\"\n" +
            "\tprincipal=\"{1}\";\n" +
            "'}';";

    static {
        configureKerberosJAAS();
    }

    public static void main(String[] args) throws InterruptedException {
        Producer<String, String> producer = createProducer(true);

        for (int i = 0; i < 100000; i++) {
            runProducer(producer, i);
            Thread.sleep(10000);
        }
    }


    private static void runProducer(Producer<String, String> producer, final int epoch) {
        long time = System.currentTimeMillis();

        for (int i = 0; i < 10; i++) {
            sendHelloMessage(producer, epoch, i);
            log.debug("send {}-{}.", epoch, i);
        }
        long elapsedTime = System.currentTimeMillis() - time;
        log.info("Epoch {} finished, cost {}ms.", epoch, elapsedTime);
    }

    private static void sendHelloMessage(Producer<String, String> producer, int epoll, int index) {
        final ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, "Epoch-" + epoll,
                "Hello: " + index);
        producer.send(record);
    }

    private static KafkaProducer<String, String> createProducer(boolean useSasl) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        if (useSasl) {
            props.put("security.protocol", "SASL_PLAINTEXT");
            props.put("sasl.mechanism", "GSSAPI");
            props.put("sasl.kerberos.service.name", "kafka");
        }
        props.put("acks", "-1");
        props.put("retries", 3);
        props.put("batch.size", 323840);
        props.put("linger.ms", 10);
        props.put("buffer.memory", 33554432);
        props.put("max.block.ms", 3000);
        props.put("connections.max.idle.ms", 10000); // 540000
        return new KafkaProducer<>(props);
    }

    private static void configureKerberosJAAS() {
        String krb5Conf = IoUtils.copyResourceToPWD("krb5.conf", "krb5.conf");
        System.setProperty("java.security.krb5.conf", krb5Conf);
        String keytabPath = IoUtils.copyResourceToPWD("deploy.keytab", "deploy.keytab");
        String content = MessageFormat.format(JAAS_TEMPLATE, keytabPath, "deploy@ZIZHIZHAN.COM");
        File jaasConf = IoUtils.saveContentToPWD(content, "kafka_jaas.conf");
        System.setProperty("java.security.auth.login.config", jaasConf.getPath());

        log.info("PWD is {}",  System.getProperty("user.dir"));
        log.info("krb5Config file is {}.", krb5Conf);
        log.info("keytab file is {}.", keytabPath);
        log.info("JAASConfig file is {}.", jaasConf.getPath());
        // System.setProperty("sun.security.krb5.debug", "true");
    }
}
