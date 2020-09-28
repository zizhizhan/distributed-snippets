
#### 数据准备

```sql
CREATE TABLE `mvcc_test` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(32) DEFAULT NULL COMMENT 'Name',
  `age` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_idx` (`name`),
  KEY `age_idx` (`age`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;

INSERT INTO mvcc_test(`name`, `age`) VALUES('james', 21);
INSERT INTO mvcc_test(`name`, `age`) VALUES('jim', 10);
INSERT INTO mvcc_test(`name`, `age`) VALUES('jam', 16);
```

#### 幻读测试

##### 客户端01

```sql
begin;
SELECT * FROM mvcc_test WHERE age > 10;

# 等待客户端02插入数据

SELECT * FROM mvcc_test WHERE age > 10;
SELECT * FROM mvcc_test WHERE age > 10 FOR UPDATE;
SELECT * FROM mvcc_test WHERE age > 10;
rollback;
```

##### 客户端01

```sql
begin;
INSERT INTO mvcc_test(`name`, `age`) VALUES('zizhi', 160);
SELECT * FROM mvcc_test WHERE age > 10;
commit;
```