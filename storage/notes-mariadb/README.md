# MariaDB

## Configure MariaDB via OSX

```bash
brew install mariadb
```

**Start MariaDB at Startup**

```bash
brew services start mariadb
```

**Start MariaDB**

```bash
mysql.server start
```

**Connect to MariaDB**

```bash
mysql -uroot
```

## Configure MariaDB via Docker

### Start MariaDB via Docker

**/etc/mysql**

```
.
├── my.cnf
├── mariadb.cnf
├── conf.d
│   ├── docker.cnf
│   └── mysqld_safe_syslog.cnf
└── mariadb.conf.d
```

**datadir**

`/var/lib/mysql`

```bash
docker run --name local-mariadb -d --rm -p 3306:3306 -e MYSQL_ROOT_PASSWORD=my-secret-pw mariadb/server:10.3

docker logs local-mariadb

docker exec -it local-mariadb bash
```

**连接到MariaDB**

可选的环境变量

- MYSQL_ROOT_PASSWORD
- MYSQL_DATABASE
- MYSQL_USER
- MYSQL_PASSWORD
- MYSQL_ALLOW_EMPTY_PASSWORD
- MYSQL_RANDOM_ROOT_PASSWORD

```bash
docker run -it --link local-mariadb:db.local --rm \
    -e MYSQL_ROOT_PASSWORD=my-secret-pw \
    mariadb sh -c 'exec mysql -hdb.local -P3306 -uroot -p"$MYSQL_ROOT_PASSWORD"'
```

### Start PhpMyAdmin

**环境变量**

- PMA_ARBITRARY - when set to 1 connection to the arbitrary server will be allowed
- PMA_HOST - define address/host name of the MySQL server
- PMA_VERBOSE - define verbose name of the MySQL server
- PMA_PORT - define port of the MySQL server
- PMA_HOSTS - define comma separated list of address/host names of the MySQL servers
- PMA_VERBOSES - define comma separated list of verbose names of the MySQL servers
- PMA_PORTS - define comma separated list of ports of the MySQL servers
- PMA_USER and PMA_PASSWORD - define username to use for config authentication method
- PMA_ABSOLUTE_URI - define user-facing URI

**连接 Linked Server**

```bash
docker run --name local-pma --rm -d --link local-mariadb:db.local -p 8080:80 \
    -e PMA_HOST=db.local \
    phpmyadmin/phpmyadmin:4.8
```

**连接 External Server**

```bash
docker run --name local-pma --rm -d \
    -e PMA_HOST=127.0.0.1 \
    -e PMA_PORT=3306 \
    -p 8080:80 phpmyadmin/phpmyadmin:4.8
```

**连接 Arbitrary Server**

```bash
docker run --name local-pma --rm -d \
    -e PMA_ARBITRARY=1 \
    -e PMA_PORT=4000 \
    -p 8080:80 phpmyadmin/phpmyadmin:4.8
```

### 停止服务

```bash
docker kill local-mariadb
docker kill local-pma
```

## Configure MariaDB via Docker Compose

```bash
docker-compose up -d

docker-compose down
```

## Restore db school

### Use command line

```bash
mysql --host=127.0.0.1 --port=3306 --user=root --password=test123 -e "SHOW DATABASES"
mysql --host=127.0.0.1 --port=3306 --user=root --password=test123 -e "SHOW VARIABLES LIKE 'character%'"
mysql --host=127.0.0.1 --port=3306 --user=root --password=test123 \
    -e "DROP DATABASE school; CREATE DATABASE school;"
mysql --host=127.0.0.1 --port=3306 --user=root --password=test123 \
    -e "ALTER DATABASE school CHARACTER SET utf8mb4;"

mysql --host=127.0.0.1 --port=3306 --user=root --password=test123 \
    -e "CREATE USER 'school'@'%' IDENTIFIED BY 'school';"
mysql --host=127.0.0.1 --port=3306 --user=root --password=test123 mysql \
    -e "GRANT ALL PRIVILEGES ON school.* TO 'school'@'%'; FLUSH PRIVILEGES;"
mysql --host=127.0.0.1 --port=3306 --user=school --password=school school < src/main/resources/sample_dbs/school/school.sql
```

### Use customize script

```bash
cd src/main/scripts

./maria start
./maria pma

./maria school drop init

./maria stop
```

## Master/Slave 模式

```bash
docker-compose -f docker-replication.yml up

mysql --host=127.0.0.1 --port=3306 --user=root --password=test123 -e "CREATE USER 'repl'@'%' IDENTIFIED BY 'repl';"
mysql --host=127.0.0.1 --port=3306 --user=root --password=test123 -e "GRANT REPLICATION SLAVE ON *.* TO 'repl'@'%';"
mysql --host=127.0.0.1 --port=3306 --user=root --password=test123 -e "SHOW MASTER STATUS\G"

docker exec -it local-mariadb-slave1 mysql -uroot -ptest123 -e \
    "CHANGE MASTER TO MASTER_HOST='db.master',\
    MASTER_PORT=3306,MASTER_USER='repl',MASTER_PASSWORD='repl',\
    MASTER_LOG_FILE='master-bin.000003',MASTER_LOG_POS=1;"

docker exec -it local-mariadb-slave1 mysql -uroot -ptest123 -e "SHOW SLAVE STATUS\G"

docker exec -it local-mariadb-slave2 mysql -uroot -ptest123 -e \
    "CHANGE MASTER TO MASTER_HOST='db.master',\
    MASTER_PORT=3306,MASTER_USER='repl',MASTER_PASSWORD='repl',\
    MASTER_LOG_FILE='master-bin.000003',MASTER_LOG_POS=1;"

docker exec -it local-mariadb-slave2 mysql -uroot -ptest123 -e "START SLAVE;"

docker exec -it local-mariadb-slave2 mysql -uroot -ptest123 -e "SHOW SLAVE STATUS\G"
```