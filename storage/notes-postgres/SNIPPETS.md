

```sql
DROP SEQUENCE IF EXISTS teachers_id_seq;
DROP SEQUENCE IF EXISTS classes_id_seq;
DROP SEQUENCE IF EXISTS courses_id_seq;
DROP SEQUENCE IF EXISTS students_id_seq;

CREATE SEQUENCE teachers_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
CREATE SEQUENCE classes_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
CREATE SEQUENCE courses_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
CREATE SEQUENCE students_id_seq
    INCREMENT BY 1
    start 10
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
```