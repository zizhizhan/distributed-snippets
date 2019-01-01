# H2 Database

## Operations

### Start

#### Using java

```bash
export MVN_REPO=`mvn help:evaluate -Dexpression=settings.localRepository | grep -v '[INFO]' | tail -n 1`

# 查看参数选项
noglob java -cp $MVN_REPO/com/h2database/h2/1.4.197/h2-1.4.197.jar org.h2.tools.Server -?

# 启动 h2 数据库并打开 Web Browser
noglob java -jar $MVN_REPO/com/h2database/h2/1.4.197/h2-1.4.197.jar

java -cp $MVN_REPO/com/h2database/h2/1.4.197/h2-1.4.197.jar org.h2.tools.Server
# TCP server running at tcp://192.168.1.3:9092 (only local connections)
# PG server running at pg://192.168.1.3:5435 (only local connections)
# Web Console server running at http://192.168.1.3:8081 (others can connect)
```

#### Using Maven

```bash
# 查看参数选项
mvn exec:java -Dexec.mainClass="org.h2.tools.Server" -Dexec.args="-?"

# 启动 h2 数据库并打开 Web Browser
mvn exec:java -Dexec.mainClass="org.h2.tools.Console"

mvn exec:java -Dexec.mainClass="org.h2.tools.Server"
mvn exec:java -Dexec.mainClass="org.h2.tools.Server" -Dexec.args="-tcpAllowOthers -webAllowOthers -webPort 8081"
```

#### Using Spring

```xml
<bean id="org.h2.tools.Server" class="org.h2.tools.Server" factory-method="createTcpServer"
    init-method="start" destroy-method="stop">
    <constructor-arg value="-tcp,-tcpAllowOthers,-tcpPort,8043" />
</bean>
```

### Shell

#### Start Shell by Maven

```bash
mvn exec:java -Dexec.mainClass="org.h2.tools.Shell"
```


### 备份与恢复

```bash
noglob java -cp $MVN_REPO/com/h2database/h2/1.4.197/h2-1.4.197.jar org.h2.tools.Backup -?
```

```sql
CREATE TABLE courses(ID INT PRIMARY KEY, NAME VARCHAR(255), TEACHER VARCHAR(255), PUBLISHER VARCHAR(255), TYPE VARCHAR(255), URL VARCHAR(255)) 
AS SELECT * FROM CSVREAD('~/opencourses.csv')
```

#### 常规备份 

```bash
java -cp $MVN_REPO/com/h2database/h2/1.4.197/h2-1.4.197.jar org.h2.tools.Script -url jdbc:h2:~/kehan -user sa -script ~/kehan.zip -options compression zip
```


#### 数据恢复

```bash
java -cp $MVN_REPO/com/h2database/h2/1.4.197/h2-1.4.197.jar org.h2.tools.RunScript -url jdbc:h2:~/kekan2 -user sa -script ~/kehan.zip -options compression zip
```

#### 在线备份

```sql
BACKUP TO 'backup.zip'
```


## Fixed

### H2的优势：

- h2采用纯Java编写，因此不受平台的限制
- h2只有一个jar文件，十分适合作为嵌入式数据库试用
- 性能和功能的优势

### H2支持三种服务模式：

- Web server：此种运行方式支持使用浏览器访问 H2 Console
- TCP server：支持客户端/服务器端的连接方式
- PG server：支持 PostgreSQL 客户端

### 连接H2数据库有以下方式

- 服务式 （Server）
  - 连接字符串示例：jdbc:h2:tcp://localhost/~/test
  - 特征：类似于传统数据库
- 嵌入式（Embedded）
  - 连接字符串示例：jdbc:h2:~/test
  - 特征：应用独占，有排他机制
- 内存（Memory）
  - 连接字符串示例：jdbc:h2:tcp://localhost/mem:test
  - 特征：数据在内存中，不落盘

---------------------

Type | URL Format | Examples
--- | --- | ---
Embedded (local) connection	| `jdbc:h2:[file:][<path>]<databaseName>` | jdbc:h2:~/test <br> jdbc:h2:file:/data/sample <br> jdbc:h2:file:C:/data/sample (Windows only)
In-memory (private)	| `jdbc:h2:mem:` |
In-memory (named) | `jdbc:h2:mem:<databaseName>` | jdbc:h2:mem:test_mem
Server mode (remote connections) using TCP/IP | `jdbc:h2:tcp://<server>[:<port>]/[<path>]<databaseName>` |  jdbc:h2:tcp://localhost/~/test <br> jdbc:h2:tcp://dbserv:8084/~/sample <br> jdbc:h2:tcp://localhost/mem:test
Server mode (remote connections) using TLS | `jdbc:h2:ssl://<server>[:<port>]/<databaseName>` | jdbc:h2:ssl://localhost:8085/~/sample;
Using encrypted files | `jdbc:h2:<url>;CIPHER=AES` | jdbc:h2:ssl://localhost/~/test;CIPHER=AES <br> jdbc:h2:file:~/secure;CIPHER=AES
File locking methods | `jdbc:h2:<url>;FILE_LOCK={FILE|SOCKET|NO}` | jdbc:h2:file:~/private;CIPHER=AES;FILE_LOCK=SOCKET
Only open if it already exists | `jdbc:h2:<url>;IFEXISTS=TRUE` | jdbc:h2:file:~/sample;IFEXISTS=TRUE
Don't close the database when the VM exits | `jdbc:h2:<url>;DB_CLOSE_ON_EXIT=FALSE` |
Execute SQL on connection | `jdbc:h2:<url>;INIT=RUNSCRIPT FROM '~/create.sql'` | jdbc:h2:file:~/sample;INIT=RUNSCRIPT FROM '~/create.sql'\;RUNSCRIPT FROM '~/populate.sql'
User name and/or password | `jdbc:h2:<url>[;USER=<username>][;PASSWORD=<value>]` | jdbc:h2:file:~/sample;USER=sa;PASSWORD=123
Debug trace settings | `jdbc:h2:<url>;TRACE_LEVEL_FILE=<level 0..3>` | jdbc:h2:file:~/sample;TRACE_LEVEL_FILE=3
Ignore unknown settings | `jdbc:h2:<url>;IGNORE_UNKNOWN_SETTINGS=TRUE`
Custom file access mode | `jdbc:h2:<url>;ACCESS_MODE_DATA=rws`
Database in a zip file | `jdbc:h2:zip:<zipFileName>!/<databaseName>` | jdbc:h2:zip:~/db.zip!/test
Compatibility mode | `jdbc:h2:<url>;MODE=<databaseType>` | jdbc:h2:~/test;MODE=MYSQL
Auto-reconnect | `jdbc:h2:<url>;AUTO_RECONNECT=TRUE` | jdbc:h2:tcp://localhost/~/test;AUTO_RECONNECT=TRUE
Automatic mixed mode | `jdbc:h2:<url>;AUTO_SERVER=TRUE` | jdbc:h2:~/test;AUTO_SERVER=TRUE
Page size | `jdbc:h2:<url>;PAGE_SIZE=512`
Changing other settings | `jdbc:h2:<url>;<setting>=<value>[;<setting>=<value>...]` | jdbc:h2:file:~/sample;TRACE_LEVEL_SYSTEM_OUT=3

---------------------

主题 | URL格式 | 范例
--- | --- | ---
本地嵌入式连接 | `jdbc:h2:[file:][<path>]<databaseName>` | jdbc:h2:~/test <br> jdbc:h2:file:/data/sample <br> jdbc:h2:file:C:/data/sample (Windows only)
内存模式（private）| `jdbc:h2:mem:` |
内存模式（named）| `jdbc:h2:mem:<databaseName>` | jdbc:h2:mem:test_mem
服务器模式（TCP/IP）| `jdbc:h2:tcp://<server>[:<port>]/[<path>]<databaseName>` | jdbc:h2:tcp://localhost/~/test <br> jdbc:h2:tcp://dbserv:8084/~/sample <br> jdbc:h2:tcp://localhost/mem:test
服务器模式（TLS）| `jdbc:h2:ssl://<server>[:<port>]/<databaseName>` | jdbc:h2:ssl://localhost:8085/~/sample;
加密方式 | `jdbc:h2:<url>;CIPHER=AES` | jdbc:h2:ssl://localhost/~/test;CIPHER=AES <br> jdbc:h2:file:~/secure;CIPHER=AES
文档锁定 | `jdbc:h2:<url>;FILE_LOCK={FILE\|SOCKET\|NO}` | jdbc:h2:file:~/private;CIPHER=AES;FILE_LOCK=SOCKET
仅存在时打开 | `jdbc:h2:<url>;IFEXISTS=TRUE` | jdbc:h2:file:~/sample;IFEXISTS=TRUE
VM存在时不关闭数据库 | `jdbc:h2:<url>;DB_CLOSE_ON_EXIT=FALSE` |
用户名、密码 | `jdbc:h2:<url>[;USER=<username>][;PASSWORD=<value>]` | jdbc:h2:file:~/sample;USER=sa;PASSWORD=123
调试日志设置 | `jdbc:h2:<url>;TRACE_LEVEL_FILE=<level 0..3>` | jdbc:h2:file:~/sample;TRACE_LEVEL_FILE=3
忽略不明设置 | `jdbc:h2:;IGNORE_UNKNOWN_SETTINGS=TRUE` |
用户文件访问 | `jdbc:h2:;ACCESS_MODE_DATA=rws` |
zip格式数据库文件 | `jdbc:h2:zip:<zipFileName>!/<databaseName>` | jdbc:h2:zip:~/db.zip!/test
兼容模式 | `jdbc:h2:<url>MODE=<databaseType>` | jdbc:h2:~/test;MODE=MYSQL
自动重新连接 | `jdbc:h2:<url>;AUTO_RECONNECT=TRUE` | jdbc:h2:tcp://localhost/~/test;AUTO_RECONNECT=TRUE
自动混合模式 | `jdbc:h2:<url>;AUTO_SERVER=TRUE` | jdbc:h2:~/test;AUTO_SERVER=TRUE
页面大小 | `jdbc:h2:<url>;PAGE_SIZE=512` |
修改其他设置 | `jdbc:h2:<url>;<setting>=<value>[;<setting>=<value>…]` | jdbc:h2:file:~/sample;TRACE_LEVEL_SYSTEM_OUT=3
