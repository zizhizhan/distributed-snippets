# PostgreSQL

Start PostgreSQL via Docker

```bash
docker run --name local-postgres --rm -v /tmp/postgres:/var/lib/postgresql/data -p 5432:5432 postgres:11-alpine

psql -h localhost -p 5432 -U postgres
```

Restore db school

```bash
psql -h localhost -p 5432 -U postgres template1 -c "CREATE DATABASE school;"
psql -h localhost -p 5432 -U postgres template1 -c "CREATE USER school;"
psql -h localhost -p 5432 -U postgres template1 -c "ALTER USER school PASSWORD 'school';"
psql -h localhost -p 5432 school < src/main/resources/sample_dbs/school/school.sql
psql -h localhost -p 5432 template1 -c "GRANT ALL on DATABASE school to school;"
psql -h localhost -p 5432 school -c "GRANT ALL on ALL tables IN SCHEMA public to school;"
```

More samples

```bash
cd src/main/scripts

./pg start

./pg employee drop init
./pg northwind drop init
./pg sakila drop init
./pg school drop init
./pg world drop init

./pg stop
```