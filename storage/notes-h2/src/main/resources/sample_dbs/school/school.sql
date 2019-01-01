BEGIN;

DROP TABLE IF EXISTS teachers;
CREATE TABLE teachers (
    id serial PRIMARY KEY,
    name varchar(32) NOT NULL,
    bio text
);

DROP TABLE IF EXISTS classes;
CREATE TABLE classes (
    id serial PRIMARY KEY,
    name varchar(32) NOT NULL UNIQUE,
    department char(64) NOT NULL
);

DROP TABLE IF EXISTS courses;
CREATE TABLE courses (
    id serial PRIMARY KEY,
    name varchar(32) NOT NULL,
    period int default 60,
    description text,
    level smallint default 0,
    status varchar(16) default 'AVAILABLE',
    teacher_id int NOT NULL,
    CONSTRAINT fk_course_teacher FOREIGN KEY(teacher_id) REFERENCES teachers(id)
);

DROP TABLE IF EXISTS students;
CREATE TABLE students (
    id serial PRIMARY KEY,
    name varchar(32) NOT NULL,
    first_name varchar(32),
    last_name varchar(32),
    class_id int,
    bio text NOT NULL,
    CONSTRAINT fk_student_class FOREIGN KEY(class_id) REFERENCES classes(id)
);

DROP TABLE IF EXISTS students_courses;
CREATE TABLE students_courses (
    student_id int NOT NULL,
    course_id int NOT NULL,
    score smallint,
    choose_time timestamp default now() NOT NULL,
    CONSTRAINT fk_student FOREIGN KEY(student_id) REFERENCES students(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_course FOREIGN KEY(course_id) REFERENCES courses(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT uk_student_course UNIQUE(student_id, course_id)
);

COMMIT;