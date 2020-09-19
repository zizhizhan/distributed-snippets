package me.jameszhan.notes.jta;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import lombok.extern.slf4j.Slf4j;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.util.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.transaction.UserTransaction;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static me.jameszhan.notes.jta.UserTransactionManagerTest.getV;
import static me.jameszhan.notes.jta.UserTransactionManagerTest.resetDb;
import static me.jameszhan.notes.jta.UserTransactionManagerTest.initDb;


/**
 * Greated by IntelliJ IDEA
 *
 * @author James Zhan
 *
 * Email: zhiqiangzhan@gmail.com
 * Date: 2019-02-14 02:03
 */
@Slf4j
public class UserTransactionTest {

    private static final String db1Url = "jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1";
    private static final String db2Url = "jdbc:h2:mem:db2;DB_CLOSE_DELAY=-1";

    private static AtomikosDataSourceBean db1DataSourceBean;
    private static AtomikosDataSourceBean db2DataSourceBean;

    @BeforeClass
    public static void setUp() throws Exception {
        JdbcDataSource ds1 = new JdbcDataSource();
        ds1.setURL(db1Url + ";TRACE_LEVEL_SYSTEM_OUT=1");

        JdbcDataSource ds2 = new JdbcDataSource();
        ds2.setURL(db2Url + ";TRACE_LEVEL_SYSTEM_OUT=1");

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
        db1DataSourceBean.close();
        db2DataSourceBean.close();
    }

    @Test
    public void xaSuccess() throws Exception {
        resetDb(db1Url, 5);
        resetDb(db2Url, 5);

        Connection conn1 = null;
        Connection conn2 = null;
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;

        log.info("JTA start...");
        UserTransaction userTransaction = new UserTransactionImp();
        try {
            userTransaction.begin();

            conn1 = db1DataSourceBean.getConnection();
            ps1 = conn1.prepareStatement("UPDATE table1 SET v = 12 WHERE id = 1");
            ps1.executeUpdate();

            conn2 = db2DataSourceBean.getConnection();
            ps2 = conn2.prepareStatement("UPDATE table1 SET v = 18 WHERE id = 1");
            ps2.executeUpdate();

            userTransaction.commit();
        } catch (Throwable t) {
            log.info("JTA catch...");
            userTransaction.rollback();
        } finally {
            IOUtils.closeSilently(conn1);
            IOUtils.closeSilently(conn2);
            IOUtils.closeSilently(ps1);
            IOUtils.closeSilently(ps2);
            log.info("JTA finally...");
        }

        Assert.assertEquals(12, getV(db1Url));
        Assert.assertEquals(18, getV(db2Url));
    }

    @Test
    public void xaFailure() throws Exception {
        resetDb(db1Url, 6);
        resetDb(db2Url, 6);

        Connection conn1 = null;
        Connection conn2 = null;
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;

        log.info("JTA start...");
        UserTransaction userTransaction = new UserTransactionImp();
        try {
            userTransaction.begin();

            conn1 = db1DataSourceBean.getConnection();
            ps1 = conn1.prepareStatement("UPDATE table1 SET v = 12 WHERE id = 1");
            ps1.executeUpdate();

            conn2 = db2DataSourceBean.getConnection();
            ps2 = conn2.prepareStatement("UPDATE table2 SET v = 18 WHERE id = 1");
            ps2.executeUpdate();

            userTransaction.commit();
        } catch (Throwable t) {
            log.info("JTA catch...");
        } finally {
            IOUtils.closeSilently(conn1);
            IOUtils.closeSilently(conn2);
            IOUtils.closeSilently(ps1);
            IOUtils.closeSilently(ps2);
            log.info("JTA finally...");
        }

        Assert.assertEquals(6, getV(db1Url));
        Assert.assertEquals(6, getV(db2Url));
    }

    @Test
    public void xaRollback() throws Exception {
        resetDb(db1Url, 7);
        resetDb(db2Url, 7);

        Connection conn1 = null;
        Connection conn2 = null;
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;

        log.info("JTA start...");
        UserTransaction userTransaction = new UserTransactionImp();
        try {
            userTransaction.begin();

            conn1 = db1DataSourceBean.getConnection();
            ps1 = conn1.prepareStatement("UPDATE table1 SET v = 12 WHERE id = 1");
            ps1.executeUpdate();

            conn2 = db2DataSourceBean.getConnection();
            ps2 = conn2.prepareStatement("UPDATE table2 SET v = 18 WHERE id = 1");
            ps2.executeUpdate();

            userTransaction.commit();
        } catch (Throwable t) {
            log.info("JTA catch...");
            userTransaction.rollback();
        } finally {
            IOUtils.closeSilently(conn1);
            IOUtils.closeSilently(conn2);
            IOUtils.closeSilently(ps1);
            IOUtils.closeSilently(ps2);
            log.info("JTA finally...");
        }

        Assert.assertEquals(7, getV(db1Url));
        Assert.assertEquals(7, getV(db2Url));
    }

}
