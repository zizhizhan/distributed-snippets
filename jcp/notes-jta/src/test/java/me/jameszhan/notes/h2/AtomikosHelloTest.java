package me.jameszhan.notes.h2;

import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import lombok.extern.slf4j.Slf4j;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.rmi.registry.LocateRegistry;
import java.sql.*;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: James Zhan
 * Email: zhiqiangzhan@gmail.com
 * Date: 2018-12-30
 * Time: 03:12
 * To change this template use File | Settings | File and Code Templates.
 */
@Slf4j
public class AtomikosHelloTest {

    private static final Properties jndiEnv = new Properties();
    private static UserTransactionManager userTransactionManager;
    private static AtomikosDataSourceBean db1DataSourceBean;
    private static AtomikosDataSourceBean db2DataSourceBean;


    @BeforeClass
    public static void setUp() throws Exception {
        LocateRegistry.createRegistry(1099);

        Class.forName("org.h2.Driver");

        jndiEnv.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
        jndiEnv.setProperty(Context.PROVIDER_URL, "rmi://localhost:1099");
        Context context = new InitialContext(jndiEnv);

        JdbcDataSource ds1 = new JdbcDataSource();
        ds1.setURL("jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1");
        context.bind("ds1", ds1);

        JdbcDataSource ds2 = new JdbcDataSource();
        ds2.setURL("jdbc:h2:mem:db2;DB_CLOSE_DELAY=-1");
        context.bind("ds2", ds2);

        userTransactionManager = new UserTransactionManager();
        userTransactionManager.setForceShutdown(true);
        userTransactionManager.init();

        db1DataSourceBean = new AtomikosDataSourceBean();
        db1DataSourceBean.setUniqueResourceName("db1");
        db1DataSourceBean.setXaDataSource(ds1);
        db1DataSourceBean.setPoolSize(10);
        db1DataSourceBean.init();

        db2DataSourceBean = new AtomikosDataSourceBean();
        db2DataSourceBean.setUniqueResourceName("db2");
        db2DataSourceBean.setXaDataSource(ds2);
        db2DataSourceBean.setPoolSize(10);
        db2DataSourceBean.init();
    }

    @AfterClass
    public static void tearDown() {
        userTransactionManager.close();
        db1DataSourceBean.close();
        db2DataSourceBean.close();
    }

    @Test
    public void xaSuccess() throws Exception {
        String db1Url = "jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1";
        String db2Url = "jdbc:h2:mem:db2;DB_CLOSE_DELAY=-1";
        initDb(db1Url);
        initDb(db2Url);

        Context ctx = new InitialContext(jndiEnv);
        DataSource ds1 = (DataSource) ctx.lookup("ds1");
        DataSource ds2 = (DataSource) ctx.lookup("ds2");

//        Connection conn1 = null;
//        Connection conn2 = null;

        try {
            userTransactionManager.begin();

            try(Connection conn1 = db1DataSourceBean.getConnection()) {
                conn1.prepareStatement("UPDATE table1 SET v = 10 WHERE id = 1").execute();
            }

            try(Connection conn2 = db2DataSourceBean.getConnection()) {
                conn2.prepareStatement("UPDATE table1 SET v = 100 WHERE id = 1").execute();
            }

            userTransactionManager.commit();
        } catch (Throwable t) {
            t.printStackTrace();
            userTransactionManager.rollback();
        } finally {
//            if (conn1 != null) {
//                conn1.close();
//            }
//            if (conn2 != null) {
//                conn2.close();
//            }
        }

        try (Connection conn = DriverManager.getConnection(db1Url)) {
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT * FROM table1 WHERE id = 1")) {
                showResultSet(rs);
            }
        }

        try (Connection conn = DriverManager.getConnection(db2Url)) {
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT * FROM table1 WHERE id = 1")) {
                showResultSet(rs);
            }
        }
    }

    private void initDb(String dbUrl) throws SQLException {
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            try (Statement st = conn.createStatement()) {
                st.execute("CREATE TABLE table1(id bigint auto_increment PRIMARY KEY, v int);");
                st.execute("INSERT INTO table1(v) VALUES(1);");
            }
        }
    }


//    @Test
//    public void testLocalStorage() throws SQLException, IOException {
//        DriverManager.getConnection("jdbc:h2:~/h2_test");
//        DriverManager.getConnection("jdbc:h2:/Users/james/h2_test2");
//        DriverManager.getConnection("jdbc:h2:file:/Users/james/h2_test3");
//
//        Server.createTcpServer().start();
//        DriverManager.getConnection("jdbc:h2:tcp://localhost/~/h2_test4");
//        DriverManager.getConnection("jdbc:h2:tcp://localhost:9092/~/h2_test5");
//
//        String[] dbFiles = { "~/h2_test.mv.db", "/Users/james/h2_test2.mv.db", "/Users/james/h2_test3.mv.db",
//                "~/h2_test4.mv.db", "~/h2_test5.mv.db" };
//        for (String dbFile : dbFiles) {
//            dbFile = dbFile.replaceFirst("^~", System.getProperty("user.home"));
//            log.info("Remove {} {}", dbFile, Files.deleteIfExists(Paths.get(dbFile)) ? "success" : "failure");
//        }
//    }
//
//    @Test
//    public void testInitDbSchoolInMemory() throws SQLException {
//        String url = "jdbc:h2:mem:test_school;DB_CLOSE_DELAY=-1";
//        initDbSchool(url);
//        try (Connection conn = DriverManager.getConnection(url)) {
//            try (Statement st = conn.createStatement();
//                 ResultSet rs = st.executeQuery("SHOW TABLES;")) {
//                showResultSet(rs);
//            }
//
//            String[] tables = { "teachers", "classes", "students", "courses", "students_courses" };
//            try (Statement statement = conn.createStatement()) {
//                for (String table : tables) {
//                    try (ResultSet rs = statement.executeQuery("SELECT * FROM " + table + ";")) {
//                        showResultSet(rs);
//                    }
//                }
//            }
//        }
//    }
//
//    @Test
//    public void testInitDbSchoolDuration() throws SQLException {
//        String url = "jdbc:h2:/tmp/test_school";
//        initDbSchool(url);
//        try (Connection conn = DriverManager.getConnection(url)) {
//            try (Statement st = conn.createStatement();
//                 ResultSet rs = st.executeQuery("SHOW TABLES;")) {
//                showResultSet(rs);
//            }
//
//            String[] tables = { "teachers", "classes", "students", "courses", "students_courses" };
//            try (Statement statement = conn.createStatement()) {
//                for (String table : tables) {
//                    try (ResultSet rs = statement.executeQuery("SELECT * FROM " + table + ";")) {
//                        showResultSet(rs);
//                    }
//                }
//            }
//        }
//    }
//
//    @Test
//    public void addAllCoursesStudent() throws SQLException {
//        String url = "jdbc:h2:/tmp/test_school";
//        initDbSchool(url);
//
//        try (Connection conn = DriverManager.getConnection(url)) {
//            List<Long> courseIds = new ArrayList<>();
//            try (Statement st = conn.createStatement()) {
//                ResultSet rs = st.executeQuery("SELECT * FROM courses");
//                while (rs.next()) {
//                    courseIds.add(rs.getLong("id"));
//                }
//            }
//
//            String studentName = "James";
//            String delSQL = "DELETE FROM students WHERE name = ?";
//            try (PreparedStatement pst = conn.prepareStatement(delSQL)) {
//                pst.setString(1, studentName);
//                pst.executeUpdate();
//            } catch (SQLException e) {
//                log.error("Delete {} failure.", studentName, e);
//            }
//
//            Long studentId = null;
//            String addSQL = "INSERT INTO students(name, class_id, bio) VALUES(?, 1, ?)";
//            try (PreparedStatement pst = conn.prepareStatement(addSQL, Statement.RETURN_GENERATED_KEYS)) {
//                pst.setString(1, studentName);
//                pst.setString(2, "James Zhan");
//                pst.executeUpdate();
//
//                ResultSet rs = pst.getGeneratedKeys();
//                if (rs.next()) {
//                    studentId = rs.getLong(1);
//                }
//            } catch (SQLException e) {
//                log.error("Add {} failure.", studentName, e);
//            }
//
//            if (studentId != null) {
//                try (Statement st = conn.createStatement()) {
//                    conn.setAutoCommit(false);
//
//                    for (Long courseId : courseIds) {
//                        st.addBatch("INSERT INTO students_courses(student_id, course_id) VALUES(" + studentId
//                                + ", " + courseId + ")");
//                    }
//
//                    int[] counts = st.executeBatch();
//                    log.info("Committed {} updates", counts.length);
//                    for (int i = 0; i < counts.length; i++) {
//                        log.info("counts[{}] = {}.", i, counts[i]);
//                    }
//                    conn.commit();
//                } catch (SQLException e) {
//                    try {
//                        log.warn(e.getMessage(), e);
//                        conn.rollback();
//                    } catch (SQLException e2) {
//                        log.error(e2.getMessage(), e2);
//                    }
//                }
//            }
//        }
//
//        whoSelectedAllCourses(url);
//
//        new File("/tmp/test_school").deleteOnExit();
//    }
//
//    /**
//     * 不存在任何一门课这些学生没有选修
//     */
//    @Test
//    public void whoSelectedAllCourses() throws SQLException {
//        String url = "jdbc:h2:/tmp/test_school";
//        initDbSchool(url);
//        whoSelectedAllCourses(url);
//    }
//
//    private void whoSelectedAllCourses(String url) throws SQLException {
//        String sql = "SELECT id, name FROM students s "
//                + "WHERE NOT EXISTS ("
//                + "  SELECT * FROM courses c WHERE NOT EXISTS ("
//                + "    SELECT * FROM students_courses sc WHERE sc.student_id = s.id AND sc.course_id = c.id"
//                + "  )"
//                + ")";
//
//        try (Connection conn = DriverManager.getConnection(url);
//             Statement statement = conn.createStatement();
//             ResultSet rs = statement.executeQuery(sql)) {
//            ResultSetMetaData meta = rs.getMetaData();
//            while (rs.next()) {
//                log.info("Get student({}: {}, {}: {}).", meta.getColumnLabel(1), rs.getString(1),
//                        meta.getColumnLabel(2), rs.getString(2));
//            }
//        }
//    }
//
//    private void initDbSchool(String url) throws SQLException {
//        String sqlPath = "sample_dbs/school/school.sql";
//        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
//        InputStream in = contextClassLoader.getResourceAsStream(sqlPath);
//        if (in == null) {
//            return;
//        }
//
//        try (Connection conn = DriverManager.getConnection(url)) {
//            RunScript.execute(conn, new InputStreamReader(in, StandardCharsets.UTF_8));
//
//            URL teacherCsv = contextClassLoader.getResource("sample_dbs/school/teachers.csv");
//            URL classCsv = contextClassLoader.getResource("sample_dbs/school/classes.csv");
//            URL studentCsv = contextClassLoader.getResource("sample_dbs/school/students.csv");
//            URL courseCsv = contextClassLoader.getResource("sample_dbs/school/courses.csv");
//            URL scCsv = contextClassLoader.getResource("sample_dbs/school/students_courses.csv");
//
//            if (teacherCsv == null || classCsv == null || studentCsv == null || courseCsv == null || scCsv == null) {
//                return;
//            }
//
//            try (Statement st = conn.createStatement()) {
//                conn.setAutoCommit(false);
//
//                st.addBatch("INSERT INTO teachers(name, bio) "
//                        + "SELECT name, bio FROM CSVREAD('" + teacherCsv + "')");
//                st.addBatch("INSERT INTO classes(name, department) "
//                        + "SELECT name, department FROM CSVREAD('" + classCsv + "')");
//                st.addBatch("INSERT INTO students(name, class_id, bio) "
//                        + "SELECT name, class_id, bio FROM CSVREAD('" + studentCsv + "')");
//                st.addBatch("INSERT INTO courses(name, period, description, status, teacher_id, level) "
//                        + "SELECT name, period, description, status, teacher_id, level FROM CSVREAD('" + courseCsv + "')");
//                st.addBatch("INSERT INTO students_courses(student_id, course_id) "
//                        + "SELECT student_id, course_id FROM CSVREAD('" + scCsv + "')");
//
//                int[] counts = st.executeBatch();
//                log.info("Committed {} updates", counts.length);
//                for (int i = 0; i < counts.length; i++) {
//                    log.info("counts[{}] = {}.", i, counts[i]);
//                }
//                conn.commit();
//            } catch (SQLException e) {
//                try {
//                    log.warn(e.getMessage(), e);
//                    conn.rollback();
//                } catch (SQLException e2) {
//                    log.error(e2.getMessage(), e2);
//                }
//            }
//
//            try (Statement st = conn.createStatement();
//                 ResultSet rs = st.executeQuery("SHOW TABLES;")) {
//                showResultSet(rs);
//            }
//        }
//    }

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
