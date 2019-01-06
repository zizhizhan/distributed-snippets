# Zookeeper 常用命令

## Zookeeper API

```bash
└──  zookeeper
     ├── create
     ├── exists
     ├── delete
     ├── setData
     ├── getData
     ├── getChildren
     ├── addAuthInfo
     ├── setACL
     ├── syscall.md
     ├── syscall.rb
     ├── syscall_map.py
     └── getACL
```

## 检查 Zookeeper 状态

### 查看哪个节点被选择作为 follower 或者 leader

```bash
echo stat | nc 127.0.0.1 2181
# Zookeeper version: 3.4.13-2d71af4dbe22557fda74f9a9b4309b15a7487f03, built on 06/29/2018 04:05 GMT
# Clients:
#  /127.0.0.1:50779[0](queued=0,recved=1,sent=0)
# 
# Latency min/avg/max: 0/0/0
# Received: 1
# Sent: 0
# Connections: 1
# Outstanding: 0
# Zxid: 0x100000016
# Mode: follower
# Node count: 5

echo stat | nc 127.0.0.1 2182
# Zookeeper version: 3.4.13-2d71af4dbe22557fda74f9a9b4309b15a7487f03, built on 06/29/2018 04:05 GMT
# Clients:
#  /127.0.0.1:50784[0](queued=0,recved=1,sent=0)
# 
# Latency min/avg/max: 0/0/26
# Received: 541
# Sent: 544
# Connections: 1
# Outstanding: 0
# Zxid: 0x100000016
# Mode: follower
# Node count: 5

echo stat | nc 127.0.0.1 2183
# Zookeeper version: 3.4.13-2d71af4dbe22557fda74f9a9b4309b15a7487f03, built on 06/29/2018 04:05 GMT
# Clients:
#  /127.0.0.1:50789[0](queued=0,recved=1,sent=0)
# 
# Latency min/avg/max: 0/0/0
# Received: 1
# Sent: 0
# Connections: 1
# Outstanding: 0
# Zxid: 0x100000016
# Mode: leader
# Node count: 5
# Proposal sizes last/min/max: 32/32/81

echo stat | nc 127.0.0.1 2184
# Zookeeper version: 3.4.13-2d71af4dbe22557fda74f9a9b4309b15a7487f03, built on 06/29/2018 04:05 GMT
# Clients:
#  /127.0.0.1:50793[0](queued=0,recved=1,sent=0)
# 
# Latency min/avg/max: 0/0/0
# Received: 1
# Sent: 0
# Connections: 1
# Outstanding: 0
# Zxid: 0x100000016
# Mode: follower
# Node count: 5

echo stat | nc 127.0.0.1 2185
# Zookeeper version: 3.4.13-2d71af4dbe22557fda74f9a9b4309b15a7487f03, built on 06/29/2018 04:05 GMT
# Clients:
#  /127.0.0.1:50797[0](queued=0,recved=1,sent=0)
# 
# Latency min/avg/max: 0/0/3
# Received: 234
# Sent: 234
# Connections: 1
# Outstanding: 0
# Zxid: 0x100000016
# Mode: follower
# Node count: 5
```

### 测试是否启动了该 Server

> 回复imok表示已经启动

```bash
echo ruok | nc 127.0.0.1 2181
# imok
```

### 列出未经处理的会话和临时节点

```bash
echo dump | nc 127.0.0.1 2181
```

### 关掉 server

```bash
echo kill | nc 127.0.0.1 2181
```

### 输出相关服务配置的详细信息

```bash
echo conf | nc 127.0.0.1 2181
```

### 列出所有连接到服务器的客户端的完全的连接

```bash
echo cons | nc 127.0.0.1 2183
```

### 输出关于服务环境的详细信息

```bash
echo envi | nc 127.0.0.1 2183
```

### 列出未经处理的请求

```bash
echo reqs | nc 127.0.0.1 2181
```

### 列出服务器 watch 的详细信息

```bash
echo wchs | nc 127.0.0.1 2183
```

### 通过 session 列出服务器 watch 的详细信息

* 如何设置 whitelist？ *
```bash
echo wchc | nc 127.0.0.1 2183
```

### 通过路径列出服务器 watch 的详细信息

* 如何设置 whitelist？ *

```bash
echo wchp | nc 127.0.0.1 2183
```



