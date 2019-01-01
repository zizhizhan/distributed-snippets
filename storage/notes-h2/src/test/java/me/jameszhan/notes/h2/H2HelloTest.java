package me.jameszhan.notes.h2;

import lombok.extern.slf4j.Slf4j;
import org.h2.tools.RunScript;
import org.junit.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: James Zhan
 * Email: zhiqiangzhan@gmail.com
 * Date: 2018-12-30
 * Time: 03:12
 * To change this template use File | Settings | File and Code Templates.
 */
@Slf4j
public class H2HelloTest {

    @BeforeClass
    public static void startServer() throws ClassNotFoundException {
        Class.forName("org.h2.Driver");
    }

    @Test
    public void testInitDbSchoolInMemory() throws SQLException {
        String url = "jdbc:h2:mem:test_school;DB_CLOSE_DELAY=-1";
        initDbSchool(url);
        try (Connection conn = DriverManager.getConnection(url)) {
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SHOW TABLES;")) {
                showResultSet(rs);
            }

            String[] tables = { "teachers", "classes", "students", "courses", "students_courses" };
            try (Statement statement = conn.createStatement()) {
                for (String table : tables) {
                    try (ResultSet rs = statement.executeQuery("SELECT * FROM " + table + ";")) {
                        showResultSet(rs);
                    }
                }
            }
        }
    }

    @Test
    public void testInitDbSchoolDuration() throws SQLException {
        String url = "jdbc:h2:/tmp/test_school";
        initDbSchool(url);
        try (Connection conn = DriverManager.getConnection(url)) {
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SHOW TABLES;")) {
                showResultSet(rs);
            }

            String[] tables = { "teachers", "classes", "students", "courses", "students_courses" };
            try (Statement statement = conn.createStatement()) {
                for (String table : tables) {
                    try (ResultSet rs = statement.executeQuery("SELECT * FROM " + table + ";")) {
                        showResultSet(rs);
                    }
                }
            }
        }
    }

    @Test
    public void addAllCoursesStudent() throws SQLException {
        String url = "jdbc:h2:/tmp/test_school";
        initDbSchool(url);

        try (Connection conn = DriverManager.getConnection(url)) {
            List<Long> courseIds = new ArrayList<>();
            try (Statement st = conn.createStatement()) {
                ResultSet rs = st.executeQuery("SELECT * FROM courses");
                while (rs.next()) {
                    courseIds.add(rs.getLong("id"));
                }
            }

            String studentName = "James";

            String delSQL = "DELETE FROM students WHERE name = ?";
            try (PreparedStatement pst = conn.prepareStatement(delSQL)) {
                pst.setString(1, studentName);
                pst.executeUpdate();
            } catch (SQLException e) {
                log.error("Delete {} failure.", studentName, e);
            }

            Long studentId = null;
            String addSQL = "INSERT INTO students(name, class_id, bio) VALUES(?, 1, ?) RETURNING id";
            try (PreparedStatement pst = conn.prepareStatement(addSQL, Statement.RETURN_GENERATED_KEYS)) {
                pst.setString(1, studentName);
                pst.setString(2, "James Zhan");
                pst.executeUpdate();

                ResultSet rs = pst.getGeneratedKeys();
                if (rs.next()) {
                    studentId = rs.getLong(1);
                }
            } catch (SQLException e) {
                log.error("Add {} failure.", studentName, e);
            }

            if (studentId != null) {
                try (Statement st = conn.createStatement()) {
                    conn.setAutoCommit(false);

                    for (Long courseId : courseIds) {
                        st.addBatch("INSERT INTO students_courses(student_id, course_id) VALUES(" + studentId
                                + ", " + courseId + ")");
                    }

                    int[] counts = st.executeBatch();
                    log.info("Committed {} updates", counts.length);
                    for (int i = 0; i < counts.length; i++) {
                        log.info("counts[{}] = {}.", i, counts[i]);
                    }
                    conn.commit();
                } catch (SQLException e) {
                    try {
                        log.warn(e.getMessage(), e);
                        conn.rollback();
                    } catch (SQLException e2) {
                        log.error(e2.getMessage(), e2);
                    }
                }
            }
        }
    }

    /**
     * 不存在任何一门课这些学生没有选修
     */
    @Test
    public void whoSelectedAllCourses() throws SQLException {
        String url = "jdbc:h2:/tmp/test_school";
        initDbSchool(url);
        String sql = "SELECT id, name FROM students s "
                + "WHERE NOT EXISTS ("
                + "  SELECT * FROM courses c WHERE NOT EXISTS ("
                + "    SELECT * FROM students_courses sc WHERE sc.student_id = s.id AND sc.course_id = c.id"
                + "  )"
                + ")";

        try (Connection conn = DriverManager.getConnection(url);
             Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                log.info("Get student({}: {}, {}: {}).", meta.getColumnLabel(1), rs.getString(1),
                        meta.getColumnLabel(2), rs.getString(2));
            }
        }
    }



//    @BeforeClass
//    public static void startServer() {
//        log.info("Start h2...");
//        try {
//            server = Server.createTcpServer().start();
//            log.info("h2 database start successful");
//        } catch (SQLException e) {
//            log.error("Start h2 error.", e);
//        }
//    }
//
//    @Before
//    public void setUp() throws ClassNotFoundException {
//        Class.forName("org.h2.Driver");
//    }
//
//    @Test
//    public void testConnectionString() throws SQLException {
//        DriverManager.getConnection("jdbc:h2:~/h2_test");
//        DriverManager.getConnection("jdbc:h2:/Users/james/h2_test2");
//        DriverManager.getConnection("jdbc:h2:file:/Users/james/h2_test3");
//        DriverManager.getConnection("jdbc:h2:tcp://localhost/~/h2_test4");
//        DriverManager.getConnection("jdbc:h2:tcp://localhost:9092/~/h2_test5");
//    }
//
//    @After
//    public void tearDown() {
//    }
//
//    @Test
//    public void useH2() throws Exception {
//        Connection conn = DriverManager.getConnection(dbDir, user, password);
//
//        Statement stat = conn.createStatement();
//        // insert data
//        stat.execute("CREATE TABLE TEST(NAME VARCHAR)");
//        stat.execute("INSERT INTO TEST VALUES('Hello World')");
//        // use data
//        ResultSet result = stat.executeQuery("select name from test ");
//        int i = 1;
//        while (result.next()) {
//            System.out.println(i++ + ":" + result.getString("name"));
//        }
//        result.close();
//        stat.close();
//        conn.close();
//    }
//
//    @AfterClass
//    public static void stopServer() {
//        if (server != null) {
//            log.info("h2 status is {}", server.getStatus());
//            log.info("Stop h2...");
//            server.stop();
//        }
//    }

    private void initDbSchool(String url) throws SQLException {
        String sqlPath = "sample_dbs/school/school.sql";
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        InputStream in = contextClassLoader.getResourceAsStream(sqlPath);
        if (in == null) {
            return;
        }

        try (Connection conn = DriverManager.getConnection(url)) {
            RunScript.execute(conn, new InputStreamReader(in, StandardCharsets.UTF_8));

            URL teacherCsv = contextClassLoader.getResource("sample_dbs/school/teachers.csv");
            URL classCsv = contextClassLoader.getResource("sample_dbs/school/classes.csv");
            URL studentCsv = contextClassLoader.getResource("sample_dbs/school/students.csv");
            URL courseCsv = contextClassLoader.getResource("sample_dbs/school/courses.csv");
            URL scCsv = contextClassLoader.getResource("sample_dbs/school/students_courses.csv");

            if (teacherCsv == null || classCsv == null || studentCsv == null || courseCsv == null || scCsv == null) {
                return;
            }

            try (Statement st = conn.createStatement()) {
                conn.setAutoCommit(false);

                st.addBatch("INSERT INTO teachers(name, bio) "
                        + "SELECT name, bio FROM CSVREAD('" + teacherCsv + "')");
                st.addBatch("INSERT INTO classes(name, department) "
                        + "SELECT name, department FROM CSVREAD('" + classCsv + "')");
                st.addBatch("INSERT INTO students(name, class_id, bio) "
                        + "SELECT name, class_id, bio FROM CSVREAD('" + studentCsv + "')");
                st.addBatch("INSERT INTO courses(name, period, description, status, teacher_id, level) "
                        + "SELECT name, period, description, status, teacher_id, level FROM CSVREAD('" + courseCsv + "')");
                st.addBatch("INSERT INTO students_courses(student_id, course_id) "
                        + "SELECT student_id, course_id FROM CSVREAD('" + scCsv + "')");

                int[] counts = st.executeBatch();
                log.info("Committed {} updates", counts.length);
                for (int i = 0; i < counts.length; i++) {
                    log.info("counts[{}] = {}.", i, counts[i]);
                }
                conn.commit();
            } catch (SQLException e) {
                try {
                    log.warn(e.getMessage(), e);
                    conn.rollback();
                } catch (SQLException e2) {
                    log.error(e2.getMessage(), e2);
                }
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SHOW TABLES;")) {
                showResultSet(rs);
            }
        }
    }

    private void showResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        while (rs.next()) {
            for (int i = 0; i < meta.getColumnCount(); i++) {
                if (i == meta.getColumnCount() - 1) {
                    log.info("{}: {}\n", meta.getColumnLabel(i + 1), rs.getString(i + 1));
                } else {
                    log.info("{}: {}", meta.getColumnLabel(i + 1), rs.getString(i + 1));
                }
            }
        }
    }

}
