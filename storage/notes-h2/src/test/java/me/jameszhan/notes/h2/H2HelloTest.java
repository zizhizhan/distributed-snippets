package me.jameszhan.notes.h2;

import lombok.extern.slf4j.Slf4j;
import org.h2.tools.Server;
import org.junit.*;

import java.sql.*;

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

    private static Server server;
    private String port = "8082";
    private String dbDir = "jdbc:h2:mem:hello_db";
    private String user = "sa";
    private String password = "";

    @BeforeClass
    public static void startServer() {
        log.info("Start h2...");
        try {
            server = Server.createTcpServer().start();
            log.info("h2 database start successful");
        } catch (SQLException e) {
            log.error("Start h2 error.", e);
        }
    }

    @Before
    public void setUp() throws ClassNotFoundException {
        Class.forName("org.h2.Driver");
    }

    @Test
    public void testConnectionString() throws SQLException {
        DriverManager.getConnection("jdbc:h2:~/h2_test");
        DriverManager.getConnection("jdbc:h2:/Users/james/h2_test2");
        DriverManager.getConnection("jdbc:h2:file:/Users/james/h2_test3");
        DriverManager.getConnection("jdbc:h2:tcp://localhost/~/h2_test4");
        DriverManager.getConnection("jdbc:h2:tcp://localhost:9092/~/h2_test5");
    }

    @After
    public void tearDown() {
    }

    @Test
    public void useH2() throws Exception {
        Connection conn = DriverManager.getConnection(dbDir, user, password);

        Statement stat = conn.createStatement();
        // insert data
        stat.execute("CREATE TABLE TEST(NAME VARCHAR)");
        stat.execute("INSERT INTO TEST VALUES('Hello World')");
        // use data
        ResultSet result = stat.executeQuery("select name from test ");
        int i = 1;
        while (result.next()) {
            System.out.println(i++ + ":" + result.getString("name"));
        }
        result.close();
        stat.close();
        conn.close();
    }

    @AfterClass
    public static void stopServer() {
        if (server != null) {
            log.info("h2 status is {}", server.getStatus());
            log.info("Stop h2...");
            server.stop();
        }
    }

}
