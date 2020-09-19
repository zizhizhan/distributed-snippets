

```bash
# AI-MAIN
zkCli -server 10.255.8.51:2181,10.255.8.53:2181,10.255.8.55:2181

[zk: 10.255.8.51:2181(CONNECTED) 2] ls /kafka-manager
[configs, mutex, deleteClusters, clusters]

[zk: 10.255.8.51:2181(CONNECTED) 2] ls /kafka-manager
[configs, mutex, deleteClusters, clusters]

[zk: 10.255.8.51:2181(CONNECTED) 6] ls /kafka_uat
[controller, controller_epoch, brokers, kafka-acl, kafka-acl-changes, admin, isr_change_notification, consumers, config]

[zk: 10.255.8.51:2181(CONNECTED) 9] ls /kafka_uat/brokers
[ids, topics, seqid]

[zk: 10.255.8.51:2181(CONNECTED) 10] ls /kafka_uat/brokers/topics
[bdp_test_topic, __consumer_offsets, ai_shopos_dc]

[zk: 10.255.8.51:2181(CONNECTED) 11] ls /kafka_uat/brokers/ids
[0, 1, 2]

[zk: 10.255.8.51:2181(CONNECTED) 29] get /kafka_uat/brokers/ids/0
{"jmx_port":9393,"timestamp":"1561468924799","endpoints":["SASL_PLAINTEXT://10.255.8.51:9092"],"host":null,"version":3,"port":-1}
cZxid = 0x600000023
ctime = Tue Jun 25 21:22:05 CST 2019
mZxid = 0x600000023
mtime = Tue Jun 25 21:22:05 CST 2019
pZxid = 0x600000023
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x16b8ecb76160003
dataLength = 129
numChildren = 0

[zk: 10.255.8.51:2181(CONNECTED) 30] get /kafka_uat/brokers/ids/1
{"jmx_port":9393,"timestamp":"1561468977598","endpoints":["SASL_PLAINTEXT://10.255.8.53:9092"],"host":null,"version":3,"port":-1}
cZxid = 0x60000002a
ctime = Tue Jun 25 21:22:57 CST 2019
mZxid = 0x60000002a
mtime = Tue Jun 25 21:22:57 CST 2019
pZxid = 0x60000002a
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x36b8ecb79dd0001
dataLength = 129
numChildren = 0

[zk: 10.255.8.51:2181(CONNECTED) 31] get /kafka_uat/brokers/ids/2
{"jmx_port":9393,"timestamp":"1561468997237","endpoints":["SASL_PLAINTEXT://10.255.8.55:9092"],"host":null,"version":3,"port":-1}
cZxid = 0x600000033
ctime = Tue Jun 25 21:23:17 CST 2019
mZxid = 0x600000033
mtime = Tue Jun 25 21:23:17 CST 2019
pZxid = 0x600000033
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x26b8ecb89a00003
dataLength = 129
numChildren = 0
```


```bash
# PUBLIC-MAIN
kafka-topics --list --bootstrap-server 10.255.10.13:9092
kafka-consumer-groups --bootstrap-server 10.255.10.13:9092 --list
```

#### 配置 Kerberos 认证

添加以下主机信息到 /etc/hosts

```conf
10.255.10.58    bdphbs040004
10.255.10.88    bdphbs040005
```

添加 `krb5.conf` 到 /etc/，并创建 `krb5.conf.d` 目录

```bash
sudo mkdir /etc/krb5.conf.d/
```

配置用户

```bash
kinit -kt hduser2010.keytab hduser2010
klist
```

# AI-MAIN
kafka-topics --list --bootstrap-server 10.255.8.51:9092
提示 Timeout

kafka-topics --zookeeper 10.255.8.51:2181/kafka_uat --list
kafka-topics --zookeeper 10.255.8.51:2181/kafka_uat --describe --topic ai_shopos_dc
```