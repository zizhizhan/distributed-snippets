

```bash
kafka-server-stop & zookeeper-server-stop
rm -fr /usr/local/var/lib/kafka-logs/* & rm -fr /usr/local/var/lib/zookeeper/*
zookeeper-server-start /usr/local/etc/kafka/zookeeper.properties & kafka-server-start /usr/local/etc/kafka/server.properties

kafka-topics --create \
    --bootstrap-server localhost:9092 \
    --replication-factor 1 \
    --partitions 1 \
    --topic streams-plaintext-input

kafka-topics --create \
    --bootstrap-server localhost:9092 \
    --replication-factor 1 \
    --partitions 1 \
    --topic streams-wordcount-output \
    --config cleanup.policy=compact

kafka-topics --bootstrap-server localhost:9092 --describe
Topic:streams-wordcount-output	PartitionCount:1	ReplicationFactor:1	Configs:cleanup.policy=compact,segment.bytes=1073741824
	Topic: streams-wordcount-output	Partition: 0	Leader: 0	Replicas: 0	Isr: 0
Topic:streams-plaintext-input	PartitionCount:1	ReplicationFactor:1	Configs:segment.bytes=1073741824
	Topic: streams-plaintext-input	Partition: 0	Leader: 0	Replicas: 0	Isr: 0
```

```bash
mvn dependency:build-classpath -DincludeScope=runtime -Dmdep.outputFile=cp.txt
cd src/main/java && javac -cp `cat ../../../cp.txt` com/zizhizhan/kafka/streams/WordCountDemo.java && cd ../../../
java -Xmx4096m -Xms4096m -Xmn2048m -Xss160k -server -cp src/main/resources:src/main/java:`cat cp.txt` com.zizhizhan.kafka.streams.WordCountDemo
```

或

```bash
mvn clean compile && mvn exec:java -Dexec.mainClass="com.zizhizhan.kafka.streams.WordCountDemo"
```

或者直接跑系统自带的用例

```bash
kafka-run-class org.apache.kafka.streams.examples.wordcount.WordCountDemo
```

```bash
kafka-console-producer --broker-list localhost:9092 --topic streams-plaintext-input
```

```bash
kafka-console-consumer --bootstrap-server localhost:9092 \
    --topic streams-wordcount-output \
    --from-beginning \
    --formatter kafka.tools.DefaultMessageFormatter \
    --property print.key=true \
    --property print.value=true \
    --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
    --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
```
