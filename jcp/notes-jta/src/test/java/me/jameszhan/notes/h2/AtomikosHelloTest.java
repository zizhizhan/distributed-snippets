package me.jameszhan.notes.h2;

import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import lombok.extern.slf4j.Slf4j;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.*;

import java.rmi.registry.LocateRegistry;
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
public class AtomikosHelloTest {

    private static final String db1Url = "jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1";
    private static final String db2Url = "jdbc:h2:mem:db2;DB_CLOSE_DELAY=-1";

    private static UserTransactionManager userTransactionManager;
    private static AtomikosDataSourceBean db1DataSourceBean;
    private static AtomikosDataSourceBean db2DataSourceBean;


    @BeforeClass
    public static void setUp() throws Exception {
        LocateRegistry.createRegistry(1099);

        JdbcDataSource ds1 = new JdbcDataSource();
        ds1.setURL(db1Url + ";TRACE_LEVEL_SYSTEM_OUT=2");

        JdbcDataSource ds2 = new JdbcDataSource();
        ds2.setURL(db2Url + ";TRACE_LEVEL_SYSTEM_OUT=2");

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

        initDb(db1Url);
        initDb(db2Url);
    }

    @AfterClass
    public static void tearDown() {
        userTransactionManager.close();
        db1DataSourceBean.close();
        db2DataSourceBean.close();
    }

    @Test
    public void xaSuccess() throws Exception {
        resetDb(db1Url, 1);
        resetDb(db2Url, 1);

        log.info("Start JTA...");
        Connection conn1 = null;
        Connection conn2 = null;
        try {
            userTransactionManager.begin();
            conn1 = db1DataSourceBean.getConnection();
            conn2 = db2DataSourceBean.getConnection();

            conn1.prepareStatement("UPDATE table1 SET v = 12 WHERE id = 1").execute();
            conn2.prepareStatement("UPDATE table1 SET v = 18 WHERE id = 1").execute();

            userTransactionManager.commit();
        } catch (Throwable t) {
            log.warn("Execute ERROR.", t);
            userTransactionManager.rollback();
        } finally {
            log.info("Ena JTA...");
            if (conn1 != null) {
                conn1.close();
            }
            if (conn2 != null) {
                conn2.close();
            }
        }

        Assert.assertEquals(12, getV(db1Url));
        Assert.assertEquals(18, getV(db2Url));
    }

    @Test
    public void xaFailure() throws Exception {
        resetDb(db1Url, 2);
        resetDb(db2Url, 2);

        log.info("Start JTA...");
        Connection conn1 = null;
        Connection conn2 = null;
        try {
            userTransactionManager.begin();

//            conn1 = DriverManager.getConnection(db1Url);
//            conn2 = DriverManager.getConnection(db2Url);
            conn1 = db1DataSourceBean.getConnection();
            conn2 = db2DataSourceBean.getConnection();

            conn1.prepareStatement("UPDATE table1 SET v = 12 WHERE id = 1").execute();
            conn2.prepareStatement("UPDATE table2 SET v = 18 WHERE id = 1").execute();

            userTransactionManager.commit();
        } catch (Throwable t) {
            log.warn("Execute ERROR.", t);
        } finally {
            log.info("Ena JTA...");
            if (conn1 != null) {
                conn1.close();
            }
            if (conn2 != null) {
                conn2.close();
            }
        }

        Assert.assertEquals(2, getV(db1Url));
        Assert.assertEquals(2, getV(db2Url));
    }

    @Test
    public void xaRollback() throws Exception {
        resetDb(db1Url, 3);
        resetDb(db2Url, 3);

        log.info("Start JTA...");
        Connection conn1 = null;
        Connection conn2 = null;
        try {
            userTransactionManager.begin();
            conn1 = db1DataSourceBean.getConnection();
            conn2 = db2DataSourceBean.getConnection();

            conn1.prepareStatement("UPDATE table1 SET v = 12 WHERE id = 1").execute();
            conn2.prepareStatement("UPDATE table2 SET v = 18 WHERE id = 1").execute();

            userTransactionManager.commit();
        } catch (Throwable t) {
            log.warn("Execute ERROR.", t);
            userTransactionManager.rollback();
        } finally {
            log.info("Ena JTA...");
            if (conn1 != null) {
                conn1.close();
            }
            if (conn2 != null) {
                conn2.close();
            }
        }

        Assert.assertEquals(3, getV(db1Url));
        Assert.assertEquals(3, getV(db2Url));
    }

    private static void initDb(String dbUrl) throws SQLException {
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            try (Statement st = conn.createStatement()) {
                st.execute("CREATE TABLE table1(id bigint auto_increment PRIMARY KEY, v int);");
                st.execute("INSERT INTO table1(v) VALUES(1);");
            }
        }
    }

    private static void resetDb(String dbUrl, int initValue) throws SQLException {
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            try (Statement st = conn.createStatement()) {
                st.execute("UPDATE table1 SET v = " + initValue + " WHERE id = 1");
            }
        }
    }

    private int getV(String dbUrl) throws Exception {
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT * FROM table1 WHERE id = 1 LIMIT 0, 1")) {
                if (rs.next()) {
                    return rs.getInt("v");
                } else {
                    return 0;
                }
            }
        }
    }

}
