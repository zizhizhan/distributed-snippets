package me.jameszhan.notes.pg;

import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.*;

/**
 * Created by IntelliJ IDEA.
 * User: James Zhan
 * Email: zhiqiangzhan@gmail.com
 * Date: 2019-01-01
 * Time: 02:17
 */
@Slf4j
public class PgHelloTest {

    @BeforeClass
    public static void registerDriver() throws Exception {
        Class.forName("org.postgresql.Driver");
    }

    @Test
    public void testDBVersion() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/postgres";
        String user = "postgres";
        String password = "postgres";

        try (Connection con = DriverManager.getConnection(url, user, password);
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT VERSION();")) {

            if (rs.next()) {
                log.info("version is {}.", rs.getArray(1));
            }
        }
    }

    /**
     * 不存在任何一门课这些学生没有选修
     */
    @Test
    public void whoSelectedAllCourses() throws SQLException {
        String sql = "SELECT id, name FROM students s "
                + "WHERE NOT EXISTS ("
                + "  SELECT * FROM courses c WHERE NOT EXISTS ("
                + "    SELECT * FROM students_courses sc WHERE sc.student_id = s.id AND sc.course_id = c.id"
                + "  )"
                + ")";

        String url = "jdbc:postgresql://localhost:5432/school";
        String user = "school";
        String password = "school";

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
