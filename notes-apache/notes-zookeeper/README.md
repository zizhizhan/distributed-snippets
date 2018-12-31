# Zookeeper 安装手札

## 1 环境版本

- 操作系统：OS X 10.14.1
- java版本: 1.8.0_45
- zookeeper版本: zookeeper-3.4.13
- docker: 18.06.1-ce
- docker-compose: 1.22.0

## 2 本地环境安装

### 2.1 安装 zookeeper

```bash
brew install zookeeper
```

### 2.2 检查默认配置

可以在 `/usr/local/etc/zookeeper` 查看 `zookeeper` 的默认配置

zoo.conf

```conf
tickTime=2000
initLimit=10
syncLimit=5
dataDir=/usr/local/var/run/zookeeper/data
clientPort=2181
```

### 2.3 单机模式

#### 2.3.1 启动 ZooKeeper 服务

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

#### 2.3.2 验证 ZooKeeper 服务

```bash
➜ zkCli -server 127.0.0.1:2181

Connecting to 127.0.0.1:2181
Welcome to ZooKeeper!
JLine support is enabled

WATCHER::

WatchedEvent state:SyncConnected type:None path:null
[zk: 127.0.0.1:2181(CONNECTED) 0] %
```

#### 2.3.3 停止 ZooKeeper 服务

```bash
zkServer stop
# ZooKeeper JMX enabled by default
# Using config: /usr/local/etc/zookeeper/zoo.cfg
# Stopping zookeeper ... STOPPED
```

### 2.4 集群模式

```bash
#master
echo "1" > /james/var/zookeeper/zoo1/data/myid

#slave1
echo "2">/usr/local/zookeeper/data/myid

#slave2
echo "3">/usr/local/zookeeper/data/myid
```

#### 2.4.1 启动 ZooKeeper 服务

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

#### 2.4.2 验证 ZooKeeper 服务

```bash
➜ zkCli -server 127.0.0.1:2181

Connecting to 127.0.0.1:2181
Welcome to ZooKeeper!
JLine support is enabled

WATCHER::

WatchedEvent state:SyncConnected type:None path:null
[zk: 127.0.0.1:2181(CONNECTED) 0] %
```

#### 2.3.3 停止 ZooKeeper 服务

```bash
zkServer stop
# ZooKeeper JMX enabled by default
# Using config: /usr/local/etc/zookeeper/zoo.cfg
# Stopping zookeeper ... STOPPED
```

### 通过 Docker

#### 单个实例

##### 启动

```bash
docker run --name zookeeper001 -d zookeeper:latest
```

##### 查询运行状态

```bash
docker ps
docker logs -f zookeeper00
```

##### 连接

```bash
docker run -it --rm --link zookeeper001:zookeeper zookeeper zkCli.sh -server zookeeper
```

##### 关闭

```bash
docker stop zookeeper001
docker rm zookeeper001
```


- ZOO_MY_ID 表示 ZK 服务的 ID，它是 1-255 之间的整数，必须在集群中唯一
- ZOO_SERVERS 表示 ZK 集群的主机列表

```bash
COMPOSE_PROJECT_NAME=zk_test docker-compose up
# 或者
COMPOSE_PROJECT_NAME=zk_test docker-compose start
```

####

```bash
COMPOSE_PROJECT_NAME=zk_test docker-compose ps
```

```bash
COMPOSE_PROJECT_NAME=zk_test docker-compose stop
COMPOSE_PROJECT_NAME=zk_test docker-compose rm

# 或者
COMPOSE_PROJECT_NAME=zk_test docker-compose down
```



docker run -it --rm \
        --link zoo1:zk1 \
        --link zoo2:zk2 \
        --link zoo3:zk3 \
        --net zk_test_default \
        zookeeper zkCli.sh -server zk1:2181,zk2:2181,zk3:2181