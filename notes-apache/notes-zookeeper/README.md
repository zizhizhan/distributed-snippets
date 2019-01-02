# Zookeeper 安装手札

## 1 环境版本

- 操作系统：OS X 10.14.1
- java版本: 1.8.0_45
- zookeeper版本: zookeeper-3.4.13
- docker: 18.06.1-ce
- docker-compose: 1.22.0

## 2 本地安装

安装 zookeeper

```bash
brew install zookeeper
```

### 2.1 单机模式

可以在 `/usr/local/etc/zookeeper` 查看 `zookeeper` 的默认配置

zoo.conf

```conf
tickTime=2000
initLimit=10
syncLimit=5
dataDir=/usr/local/var/run/zookeeper/data
clientPort=2181
```

#### 2.1.1 启动 ZooKeeper 服务

```bash
zkServer start
# ZooKeeper JMX enabled by default
# Using config: /usr/local/etc/zookeeper/zoo.cfg
# Starting zookeeper ... STARTED

zkServer status
# ZooKeeper JMX enabled by default
# Using config: /usr/local/etc/zookeeper/zoo.cfg
# Mode: standalone
```

#### 2.1.2 验证 ZooKeeper 服务

```bash
zkCli -server 127.0.0.1:2181
```

#### 2.1.3 停止 ZooKeeper 服务

```bash
zkServer stop
# ZooKeeper JMX enabled by default
# Using config: /usr/local/etc/zookeeper/zoo.cfg
# Stopping zookeeper ... STOPPED
```

### 2.2 集群模式

下面演示3个实例的配置

`/tmp/zoo/zk1/zoo.cfg`

```ini
tickTime=2000
initLimit=10
syncLimit=5
dataDir=/tmp/zoo/zk1/data
dataLogDir=/tmp/zoo/zk1/logs
clientPort=2181

server.1=localhost:2888:3888
server.2=localhost:2889:3889
server.3=localhost:2890:3890
```

`/tmp/zoo/zk2/zoo.cfg`

```ini
tickTime=2000
initLimit=10
syncLimit=5
dataDir=/tmp/zoo/zk2/data
dataLogDir=/tmp/zoo/zk2/logs
clientPort=2182

server.1=localhost:2888:3888
server.2=localhost:2889:3889
server.3=localhost:2890:3890
```

`/tmp/zoo/zk3/zoo.cfg`

```conf
tickTime=2000
initLimit=10
syncLimit=5
dataDir=/tmp/zoo/zk3/data
dataLogDir=/tmp/zoo/zk3/logs
clientPort=2183

server.1=localhost:2888:3888
server.2=localhost:2889:3889
server.3=localhost:2890:3890
```

#### 2.2.1 启动 ZooKeeper 服务

```bash
echo "1" > /tmp/zoo/zk1/data/myid
zkServer start /tmp/zoo/zk1/zoo.cfg

echo "2" > /tmp/zoo/zk2/data/myid
zkServer start /tmp/zoo/zk2/zoo.cfg

echo "3" > /tmp/zoo/zk3/data/myid
zkServer start /tmp/zoo/zk3/zoo.cfg
```

或者

```bash
src/main/scripts/zookeeper cluster start /tmp/zoo
```

#### 2.2.2 验证 ZooKeeper 服务

```bash
zkCli -server 127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183
```

#### 2.2.3 停止 ZooKeeper 服务

```bash
zkServer stop /tmp/zoo/zk1/zoo.cfg
zkServer stop /tmp/zoo/zk2/zoo.cfg
zkServer stop /tmp/zoo/zk3/zoo.cfg
```

或

```bash
src/main/scripts/zookeeper cluster stop /tmp/zoo
```

## 3. Docker 安装

### 3.1 单个实例

#### 3.1.1 启动 ZooKeeper 服务

```bash
docker run -p 2182:2181 --name zookeeper001 -d zookeeper:latest
```

#### 3.1.2 查询运行状态

```bash
docker ps
docker logs -f zookeeper001

# 连接
docker run -it --rm --link zookeeper001:zookeeper zookeeper zkCli.sh -server zookeeper
# 或
zkCli -server 127.0.0.1:2182
```

#### 3.1.3 停止 ZooKeeper 服务

```bash
docker stop zookeeper001
docker rm zookeeper001
```

### 3.2 集群模式

```yaml
version: '3'
services:
  zoo1:
    image: zookeeper
    restart: always
    container_name: zoo1
    ports:
      - '2181:2181'
    environment:
      ZOO_MY_ID: 1
      ZOO_SERVERS: server.1=zoo1:2888:3888 server.2=zoo2:2888:3888 server.3=zoo3:2888:3888
  zoo2:
    image: zookeeper
    restart: always
    container_name: zoo2
    ports:
      - "2182:2181"
    environment:
      ZOO_MY_ID: 2
      ZOO_SERVERS: server.1=zoo1:2888:3888 server.2=zoo2:2888:3888 server.3=zoo3:2888:3888
  zoo3:
    image: zookeeper
    restart: always
    container_name: zoo3
    ports:
      - "2183:2181"
    environment:
      ZOO_MY_ID: 3
      ZOO_SERVERS: server.1=zoo1:2888:3888 server.2=zoo2:2888:3888 server.3=zoo3:2888:3888
```

> ZOO_MY_ID 表示 ZK 服务的 ID，它是 1-255 之间的整数，必须在集群中唯一，
> ZOO_SERVERS 表示 ZK 集群的主机列表

#### 3.2.1 启动 ZooKeeper 服务

```bash
COMPOSE_PROJECT_NAME=zk_test docker-compose up
# 或者
COMPOSE_PROJECT_NAME=zk_test docker-compose start
```

#### 3.2.2 查询运行状态

```bash
docker ps
docker logs -f zoo1
docker logs -f zoo2
docker logs -f zoo3
COMPOSE_PROJECT_NAME=zk_test docker-compose ps

# 连接
docker run -it --rm \
        --link zoo1:zk1 \
        --link zoo2:zk2 \
        --link zoo3:zk3 \
        --net zk_test_default \
        zookeeper zkCli.sh -server zk1:2181,zk2:2181,zk3:2181
# 或
zkCli -server 127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183
```

#### 3.2.3 关闭 ZooKeeper 服务

```bash
COMPOSE_PROJECT_NAME=zk_test docker-compose stop
COMPOSE_PROJECT_NAME=zk_test docker-compose rm

# 或者
COMPOSE_PROJECT_NAME=zk_test docker-compose down
```