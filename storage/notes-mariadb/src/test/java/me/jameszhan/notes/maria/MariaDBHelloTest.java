package me.jameszhan.notes.maria;

import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: James Zhan
 * Email: zhiqiangzhan@gmail.com
 * Date: 2019-01-01
 * Time: 02:17
 */
@Slf4j
public class MariaDBHelloTest {

    @BeforeClass
    public static void registerDriver() throws Exception {
        Class.forName("org.mariadb.jdbc.Driver");
    }

    @Test
    public void testDBVersion() throws SQLException {
        String url = "jdbc:mariadb://localhost:3306/mysql";
        String user = "root";
        String password = "test123";

        try (Connection con = DriverManager.getConnection(url, user, password);
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT VERSION();")) {

            if (rs.next()) {
                log.info("version is {}.", rs.getString(1));
            }
        }
    }

    /**
     * ./maria school drop init
     *
     * @throws SQLException
     */
    @Test
    public void addAllCoursesStudent() throws SQLException {
        String url = "jdbc:mariadb://localhost:3306/school";
        String user = "school";
        String password = "school";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            List<Long> courseIds = new ArrayList<>();
            try (Statement st = conn.createStatement()) {
                ResultSet rs = st.executeQuery("SELECT * FROM courses");
                while (rs.next()) {
                    courseIds.add(rs.getLong("id"));
                }
            }

            String studentName = "James";
            Long originStudentId = null;
            String querySQL = "SELECT id FROM students WHERE name = ? LIMIT 1";
            try (PreparedStatement pst = conn.prepareStatement(querySQL)) {
                pst.setString(1, studentName);
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        originStudentId = rs.getLong(1);
                    }
                }
            } catch (SQLException e) {
                log.error("Query {} failure.", studentName, e);
            }

            if (originStudentId != null) {
                try (Statement st = conn.createStatement()) {
                    conn.setAutoCommit(false);
                    st.addBatch("DELETE FROM students_courses WHERE student_id = " + originStudentId);
                    st.addBatch("DELETE FROM students WHERE id = " + originStudentId);
                    int[] counts = st.executeBatch();
                    log.info("Committed {} updates for deleted {}.", counts.length, originStudentId);
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

            Long studentId = null;
            String addSQL = "INSERT INTO students(name, class_id, bio) VALUES(?, 1, ?)";
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
        String url = "jdbc:mariadb://localhost:3306/school";
        String user = "school";
        String password = "school";

        String sql = "SELECT id, name FROM students s "
                + "WHERE NOT EXISTS ("
                + "  SELECT * FROM courses c WHERE NOT EXISTS ("
                + "    SELECT * FROM students_courses sc WHERE sc.student_id = s.id AND sc.course_id = c.id"
                + "  )"
                + ")";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                log.info("Get student({}: {}, {}: {}).", meta.getColumnLabel(1), rs.getString(1),
                        meta.getColumnLabel(2), rs.getString(2));
            }
        }
    }

}
