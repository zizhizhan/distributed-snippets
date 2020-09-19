

```bash
export KAFKA_OPTS="-Djava.security.auth.login.config=`pwd`/jaas.conf"

kafka-topics --list --bootstrap-server 10.255.8.51:9092,10.255.8.53:9092,10.255.8.55:9092

kafka-console-consumer --bootstrap-server 10.255.8.51:9092,10.255.8.53:9092,10.255.8.55:9092 \
--topic ai_shopos_dc --from-beginning --consumer.config ./consumer.properties

kafka-console-producer --broker-list 10.255.8.51:9092,10.255.8.53:9092,10.255.8.55:9092 \
--topic ai_shopos_dc --producer.config ./producer.properties
```