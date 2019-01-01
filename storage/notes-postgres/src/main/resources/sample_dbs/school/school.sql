BEGIN;

SET client_encoding = 'UTF8';

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
    description text NOT NULL,
    level smallint default 0,
    status varchar(16) default 'AVAILABLE',
    teacher_id int NOT NULL,
    leading_course_ids int[],
    books text[],
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



COPY teachers(id, name, bio) FROM stdin WITH DELIMITER '|';
1|张老师|毁人不倦
2|李老师|坑人不止
3|王老师|兵不血刃
\.

COPY classes(id, name, department) FROM stdin WITH DELIMITER '|';
1|2018计算机软件与理论|计算机学院
2|2018计算机应用技术|计算机学院
3|2018计算机系统结构|计算机学院
4|2018信息安全|计算机学院
5|2018软件工程|计算机学院
6|2018安防应急信息技术|计算机学院
7|2018通信与信息系统|计算机学院
8|2018模式识别与智能系统|计算机学院
9|2018电子与通信工程|计算机学院
\.

COPY students(id, name, class_id, bio) FROM stdin WITH DELIMITER '|';
1|赵大|1|赵大
2|李二|1|李二
3|张三|1|张三
4|李四|1|李四
5|王五|1|王五
6|马六|3|马六
7|田七|3|田七
\.

COPY courses(id, name, period, description, status, teacher_id, level, leading_course_ids, books) FROM stdin WITH DELIMITER '|';
1|高等数学|120|机器学习依赖课程|已审核|1|0|{}|{}
2|计算机导论|30||已审核|3|0|{}|{}
3|Python程序设计|30||已审核|3|1|{2}|{}
4|Java程序设计|30||已审核|3|1|{2}|{}
5|C语言程序设计|30||已审核|3|1|{2}|{'K&R'}
6|离散数学|80|计算机科学依赖课程|已审核|2|2|{1}|{}
7|概率论与数理统计|80|数据科学依赖课程|已审核|1|2|{1,3}|{}
8|C++程序设计|50||已审核|3|2|{5}|{'C++ Primer'}
9|C#程序设计|30||已审核|3|2|{3,4,5}|{}
10|数据结构与算法|30||已审核|3|2|{3,4,5,8,9}|{'算法导论'}
11|线性代数|80|机器学习及图形科学依赖课程|已审核|1|2|{}|{}
12|计算机组成原理|30||已审核|3|2|{2}|{'深入理解计算机系统'}
13|编译原理|30||已审核|3|3|{10}|{'计算机程序的构造和解释','龙书'}
14|软件工程|30||已审核|3|3|{10}|{}
15|操作系统|30||已审核|3|3|{5,12}|{'操作系统设计与实现','Tanenbaum 的书','操作系统：精髓与设计原理'}
16|计算机网络|30||已审核|3|3|{15}|{}
17|数据库系统原理|30||已审核|3|3|{10}|{}
18|算法分析与设计|30||已审核|3|2|{6,7,10}|{}
19|软件测试|30||已审核|3|3|{4,7}|{}
20|软件测试方法和技术实践|30||已审核|3|4|{14,15,19}|{}
21|面向对象软件开发实践|30||已审核|3|4|{14,15}|{'设计模式'}
22|计算机系统结构|30||已审核|3|4|{13,15}|{'计算机组成与软硬件接口','计算机系统结构：一种量化研究方法','David Patterson 和 John Hennessy 合写的书'}
23|Linux内核分析|30||已审核|3|4|{15}|{}
24|软件安全|30||已审核|3|4|{15,16,19}|{}
25|信息安全数学基础|30||已审核|3|4|{1,11}|{}
26|抽象代数|30||已审核|3|4|{6,11}|{}
27|数理逻辑|80|计算机科学依赖课程|已审核|1|0|{}|{}
28|实变函数|50|机器学习依赖课程|审核中|2|10|{}|{}
29|泛函分析|50|机器学习依赖课程|审核中|2|10|{}|{}
30|常微分方程|50|机器学习依赖课程|审核中|2|10|{}|{}
31|偏微分方程|50|机器学习依赖课程|审核中|2|10|{}|{}
32|复变函数|50|机器学习依赖课程|审核中|2|10|{}|{}
33|拓扑学|50|机器学习依赖课程|审核中|2|10|{}|{}
34|数值分析|50|机器学习依赖课程|审核中|2|10|{}|{}
35|矩阵论|50|机器学习依赖课程|审核中|2|10|{}|{}
36|傅立叶分析|50|机器学习依赖课程|审核中|2|10|{}|{}
37|凸优化|50|机器学习依赖课程|审核中|2|10|{}|{}
38|高等概率论|50|机器学习依赖课程|审核中|2|10|{}|{}
39|多元统计|50|机器学习依赖课程|审核中|2|10|{}|{}
40|非参数统计|50|机器学习依赖课程|审核中|2|10|{}|{}
41|贝叶斯统计|50|机器学习依赖课程|审核中|2|10|{}|{}
42|随机过程|50|机器学习依赖课程|审核中|2|10|{}|{}
43|回归分析|50|机器学习依赖课程|审核中|2|10|{}|{}
44|信息论|50|机器学习依赖课程|审核中|2|10|{}|{}
45|图论|50|机器学习依赖课程|审核中|2|10|{}|{}
46|数字逻辑|50||审核中|2|1|{}|{'编码的奥秘'}
\.

COPY students_courses(student_id, course_id) FROM stdin WITH DELIMITER '|';
1|1
1|2
1|3
1|4
1|5
1|6
1|7
1|8
1|9
2|26
2|27
2|28
2|29
2|30
2|31
2|32
2|33
2|34
2|35
2|36
2|37
2|38
2|39
2|40
2|41
2|42
2|43
2|44
2|45
2|46
3|10
3|11
3|12
3|13
3|14
3|15
3|16
3|17
3|18
3|19
3|20
3|21
3|22
3|23
3|24
7|1
7|2
7|3
7|4
7|5
7|6
7|7
7|8
7|9
7|10
7|11
7|12
7|13
7|14
7|15
7|16
7|17
7|18
7|19
7|20
7|21
7|22
7|23
7|24
7|25
7|26
7|27
7|28
7|29
7|30
7|31
7|32
7|33
7|34
7|35
7|36
7|37
7|38
7|39
7|40
7|41
7|42
7|43
7|44
7|45
7|46
\.


SELECT setval('teachers_id_seq', (SELECT MAX(id) from "teachers"));
SELECT setval('students_id_seq', (SELECT MAX(id) from "students"));
SELECT setval('classes_id_seq', (SELECT MAX(id) from "classes"));
SELECT setval('courses_id_seq', (SELECT MAX(id) from "courses"));

GRANT SELECT ON teachers_id_seq TO school;
GRANT SELECT ON students_id_seq TO school;
GRANT SELECT ON classes_id_seq TO school;
GRANT SELECT ON courses_id_seq TO school;

GRANT UPDATE ON teachers_id_seq TO school;
GRANT UPDATE ON students_id_seq TO school;
GRANT UPDATE ON classes_id_seq TO school;
GRANT UPDATE ON courses_id_seq TO school;

ANALYZE teachers;
ANALYZE students;
ANALYZE classes;
ANALYZE courses;
ANALYZE students_courses;
