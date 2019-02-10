USE school;

BEGIN;

DROP TABLE IF EXISTS students_courses;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS classes;
DROP TABLE IF EXISTS teachers;

CREATE TABLE teachers (
    id serial PRIMARY KEY,
    name varchar(32) NOT NULL,
    bio text
) ENGINE = InnoDB;

CREATE TABLE classes (
    id serial PRIMARY KEY,
    session int default 60,
    name varchar(32) NOT NULL UNIQUE,
    department char(64) NOT NULL
) ENGINE = InnoDB;

CREATE TABLE courses (
    id serial PRIMARY KEY,
    name varchar(32) NOT NULL,
    period int default 60,
    description text NOT NULL,
    level smallint default 0,
    status varchar(16) default 'AVAILABLE',
    teacher_id bigint unsigned NOT NULL,
    CONSTRAINT fk_course_teacher FOREIGN KEY(teacher_id) REFERENCES teachers(id)
) ENGINE = InnoDB;

CREATE TABLE students (
    id serial PRIMARY KEY,
    name varchar(32) NOT NULL,
    first_name varchar(32),
    last_name varchar(32),
    class_id bigint unsigned,
    bio text NOT NULL,
    CONSTRAINT fk_student_class FOREIGN KEY(class_id) REFERENCES classes(id)
) ENGINE = InnoDB;

CREATE TABLE students_courses (
    student_id bigint unsigned  NOT NULL,
    course_id bigint unsigned  NOT NULL,
    score smallint,
    choose_time timestamp default now() NOT NULL,
    CONSTRAINT fk_student FOREIGN KEY(student_id) REFERENCES students(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_course FOREIGN KEY(course_id) REFERENCES courses(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT uk_student_course UNIQUE(student_id, course_id)
) ENGINE = InnoDB;

COMMIT;

LOAD DATA LOCAL INFILE '$$PATH$$/teachers.csv' INTO TABLE teachers fields terminated BY ',' enclosed BY '"' lines terminated BY '\n' IGNORE 1 LINES (`name`, `bio`);
LOAD DATA LOCAL INFILE '$$PATH$$/classes.csv' INTO TABLE classes fields terminated BY ',' enclosed BY '"' lines terminated BY '\n' IGNORE 1 LINES (`session`, `name`, `department`);
LOAD DATA LOCAL INFILE '$$PATH$$/courses.csv' INTO TABLE courses fields terminated BY ',' enclosed BY '"' lines terminated BY '\n' IGNORE 1 LINES (`name`, `period`, `description`, `level`, `status`, `teacher_id`);
LOAD DATA LOCAL INFILE '$$PATH$$/students.csv' INTO TABLE students fields terminated BY ',' enclosed BY '"' lines terminated BY '\n' IGNORE 1 LINES (`name`, `first_name`, `last_name`, `class_id`, `bio`);
LOAD DATA LOCAL INFILE '$$PATH$$/students_courses.csv' INTO TABLE students_courses fields terminated BY ',' enclosed BY '"' lines terminated BY '\n' IGNORE 1 LINES (`student_id`, `course_id`);
