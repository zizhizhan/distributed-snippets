# PostgreSQL

Start PostgreSQL via Docker

```bash
docker run --name local-postgres --rm -v /tmp/postgres:/var/lib/postgresql/data -p 5432:5432 postgres:11-alpine

psql -h localhost -p 5432 -U postgres

```

Restore db school

```bash
psql -h localhost -p 5432 -U postgres -d school -f src/test/resources/school.sql

```

