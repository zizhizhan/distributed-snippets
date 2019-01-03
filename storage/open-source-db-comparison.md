# OpenSource Database Comparison

&nbsp;                  | H2         | HSQLDB     | DERBY      | PostgreSQL | MySQL
----------------------- | ---------- | ---------- | ---------- | ---------- | ----------
ACID                    | no(ACI)    | no         | yes        | yes        | yes
referential integrity   | yes        | yes        | yes        | yes        | yes
transactions            | yes        | yes        | yes        | yes        | yes
unicode                 | yes        | yes        | yes        | yes        | partial
interface               | SQL        | SQL        | SQL        | SQL        | SQL
data size DB Limit      | 256G       | 8G         | unlimited  | unlimited  | unlimited
temporary table         | yes        | yes        | yes        | yes        | yes
materialized view       | no         | no         | no         | no         | no
indexes (R-/R+ tree)    | no         | no         | no         | yes        | MyISAM tables only
union                   | yes        | yes        | yes        | yes        | yes
intersect               |            |            |            | yes        | no
expect                  |            |            |            | yes        | no
inner join              | yes        | yes        | yes        | yes        | yes
outer join              | yes        | yes        | yes        | yes        | yes
blobs and clobs         | yes        |            | yes        | yes        | yes
data domain             | yes        |            | no         | yes        | no
cursor                  | no         | no         | yes        | yes        | yes
trigger                 | yes        | yes        | yes        | yes        | yes
function                | yes        | yes        | yes        | yes        | yes
procedure               | yes        | yes        | yes        | yes        | yes
external routine        | yes        | yes        | yes        | yes        | yes
maintainer              | H2 Software | HSQL Development Group | Apache | PostgreSQL Global Development Group | Sun Microsystems
latest stable version   | 1.1.107    | 1.8.0      | 10.4.2.0   | 8.3.6       | 5.1.32
software license        | EPL and modified MPL | BSD | Apache License | BSD | GPL or proprietary
OS support Windows      | yes        | yes        | yes        | yes        | yes
OS support Mac OS X     | yes        | yes        | yes        | yes        | yes
OS support Linux        | yes        | yes        | yes        | yes        | yes
OS support BSD          | yes        | yes        | yes        | yes        | yes
OS support UNIX         | yes        | yes        | yes        | yes        | yes
OS support Symbian      | no         | no         | no         | yes        | yes
Homepage                | www.h2database.com | www.hsqldb.org | db.apache.org/derby | www.postgresql.org | www.mysql.com

Feature                           | H2         | Derby      | HSQLDB     | MySQL      | PostgreSQL
--------------------------------- | ---------- | ---------- | ---------- | ---------- | ----------
Pure Java                         | Yes        | Yes        | Yes        | No         | No
Embedded Mode (Java)              | Yes        | Yes        | Yes        | No         | No
In-Memory Mode                    | Yes        | Yes        | Yes        | No         | No
Explain Plan                      | Yes        | Yes *12    | Yes        | Yes        | Yes
Built-in Clustering / Replication | Yes        | Yes        | No         | Yes        | Yes
Encrypted Database                | Yes        | Yes *10    | Yes *10    | No         | No
Linked Tables                     | Yes        | No         | Partially *1 | Partially *2 | Yes
ODBC Driver                       | Yes        | No         | No         | Yes        | Yes
Fulltext Search                   | Yes        | Yes        | No         | Yes        | Yes
Domains (User-Defined Types)      | Yes        | No         | Yes        | Yes        | Yes
Files per Database                | Few        | Many       | Few        | Many       | Many
Row Level Locking                 | Yes *9     | Yes        | Yes *9     | Yes        | Yes
Multi Version Concurrency         | Yes        | No         | Yes        | Yes        | Yes
Multi-Threaded Processing         | No *11     | Yes        | Yes        | Yes        | Yes
Role Based Security               | Yes        | Yes *3     | Yes        | Yes        | Yes
Updatable Result Sets             | Yes        | Yes *7     | Yes        | Yes        | Yes
Sequences                         | Yes        | Yes        | Yes        | No         | Yes
Limit and Offset                  | Yes        | Yes *13    | Yes        | Yes        | Yes
Window Functions                  | No *15     | No *15     | No         | No         | Yes
Temporary Tables                  | Yes        | Yes *4     | Yes        | Yes        | Yes
Information Schema                | Yes        | No *8      | Yes        | Yes        | Yes
Computed Columns                  | Yes        | Yes        | Yes        | Yes        | Yes *6
Case Insensitive Columns          | Yes        | Yes *14    | Yes        | Yes        | Yes *6
Custom Aggregate Functions        | Yes        | No         | Yes        | No         | Yes
CLOB/BLOB Compression             | Yes        | No         | No         | No         | Yes
Footprint (jar/dll size)          | ~1.5 MB *5 | ~3 MB      | ~1.5 MB    | ~4 MB      | ~6 MB

- *1 HSQLDB supports text tables.
- *2 MySQL supports linked MySQL tables under the name 'federated tables'.
- *3 Derby support for roles based security and password checking as an option.
- *4 Derby only supports global temporary tables.
- *5 The default H2 jar file contains debug information, jar files for other databases do not.
- *6 PostgreSQL supports functional indexes.
- *7 Derby only supports updatable result sets if the query is not sorted.
- *8 Derby doesn't support standard compliant information schema tables.
- *9 When using MVCC (multi version concurrency).
- *10 Derby and HSQLDB don't hide data patterns well.
- *11 The MULTI_THREADED option is not enabled by default, and with version 1.3.x not supported when using MVCC.
- *12 Derby doesn't support the EXPLAIN statement, but it supports runtime statistics and retrieving statement execution - plans.
- *13 Derby doesn't support the syntax LIMIT .. [OFFSET ..], however it supports FETCH FIRST .. ROW[S] ONLY.
- *14 Using collations. *15 Derby and H2 support ROW_NUMBER() OVER().