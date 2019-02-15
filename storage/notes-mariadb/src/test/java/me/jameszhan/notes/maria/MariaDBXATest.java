package me.jameszhan.notes.maria;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mariadb.jdbc.MariaDbConnection;
import org.mariadb.jdbc.MariaDbXid;
import org.mariadb.jdbc.MariaXaConnection;

import javax.sql.XAConnection;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.sql.*;

/**
 * `docker-compose -f docker-replica.yml up`
 *
 * @author James Zhan
 *
 * Email: zhiqiangzhan@gmail.com
 * Date: 2019-02-16 00:22
 */
@Slf4j
public class MariaDBXATest {

    private static final String USER = "root";
    private static final String PASS = "test123";

    // AP请求TM执行一个分布式事务，TM生成全局事务id
    private static final byte[] GTRID = "gtrid-123456".getBytes();
    private static final int FORMAT_ID = 1;

    @Test
    public void testSuccess() throws SQLException {
        runTest("INSERT into user(name) VALUES ('user1')",
                "INSERT into user(name) VALUES ('user2')",
                "INSERT into user(name) VALUES ('user3')",
                "user1",
                "user2",
                "user3");
    }

    @Test
    public void testRollback() throws SQLException {
        runTest("INSERT into user(name) VALUES ('user1')",
                "INSERT into user(name) VALUES ('user2')",
                "INSERT into user(name) VALUES ('user3user3user3user3user3user3')",
                null,
                null,
                null);
    }

    @BeforeClass
    public static void beforeClass() throws SQLException {
        createDataBase("jdbc:mariadb://localhost:3306/", "test");
        createDataBase("jdbc:mariadb://localhost:3307/", "test");
        createDataBase("jdbc:mariadb://localhost:3308/", "test");
    }

    private void runTest(String sql1,
                         String sql2,
                         String sql3,
                         String expectResult1,
                         String expectResult2,
                         String expectResult3) throws SQLException {
        // 获得资源管理器操作接口实例 RM1
        Connection conn1 = DriverManager.getConnection("jdbc:mariadb://localhost:3306/test", USER, PASS);
        XAConnection xaConn1 = new MariaXaConnection((MariaDbConnection) conn1);
        XAResource rm1 = xaConn1.getXAResource();

        // 获得资源管理器操作接口实例 RM2
        Connection conn2 = DriverManager.getConnection("jdbc:mariadb://localhost:3307/test", USER, PASS);
        XAConnection xaConn2 = new MariaXaConnection((MariaDbConnection) conn2);
        XAResource rm2 = xaConn2.getXAResource();

        // 获得资源管理器操作接口实例 RM3
        Connection conn3 = DriverManager.getConnection("jdbc:mariadb://localhost:3308/test", USER, PASS);
        XAConnection xaConn3 = new MariaXaConnection((MariaDbConnection) conn3);
        XAResource rm3 = xaConn3.getXAResource();

        log.info("Create RMs successful.");
        try {
            runTest(rm1, conn1, sql1, rm2, conn2, sql2, rm3, conn3, sql3);
            String result1 = getName(conn1), result2 = getName(conn2), result3 = getName(conn3);
            log.info("Get results [{},{},{}].", result1, result2, result3);
            Assert.assertEquals(expectResult1, result1);
            Assert.assertEquals(expectResult2, result2);
            Assert.assertEquals(expectResult3, result3);
        } finally {
            log.info("Close connections.");
            xaConn1.close();
            xaConn2.close();
            xaConn3.close();
        }
    }

    private void runTest(XAResource rm1, Connection conn1, String sql1,
                        XAResource rm2, Connection conn2, String sql2,
                        XAResource rm3, Connection conn3, String sql3) {

        Xid xid1 = null, xid2 = null, xid3 = null;
        try {

            // ==============分别执行 RM1, RM2 和 RM3 上的事务分支====================
            log.info("Start to execute RM branches.");
            // TM生成rm1上的事务分支id
            xid1 = createXid("b00001");
            // 执行rm1上的事务分支
            executeBranch(rm1, xid1, conn1, sql1);

            // TM生成rm2上的事务分支id
            xid2 = createXid("b00002");
            // 执行rm2上的事务分支
            executeBranch(rm2, xid2, conn2, sql2);

            // TM生成rm3上的事务分支id
            xid3 = createXid("b00003");
            // 执行rm2上的事务分支
            executeBranch(rm3, xid3, conn3, sql3);
            log.info("End to execute RM branches.");

            // ===================两阶段提交================================
            // phase1：询问所有的RM 准备提交事务分支
            log.info("Start to prepare RMs.");
            int rm1PrepareResult = rm1.prepare(xid1);
            int rm2PrepareResult = rm2.prepare(xid2);
            int rm3PrepareResult = rm3.prepare(xid3);
            log.info("End to prepare RMs.");

            // phase2：提交所有事务分支
            boolean onePhase = false; // TM判断有3个事务分支，所以不能优化为一阶段提交
            if (rm1PrepareResult == XAResource.XA_OK
                    && rm2PrepareResult == XAResource.XA_OK
                    && rm3PrepareResult == XAResource.XA_OK) { // 所有事务分支都prepare成功，提交所有事务分支
                log.info("Start to commit RMs.");
                rm1.commit(xid1, onePhase);
                rm2.commit(xid2, onePhase);
                rm3.commit(xid3, onePhase);
                log.info("End to commit RMs.");
            } else { // 如果有事务分支没有成功，则回滚
                log.info("Start to rollback RMs.");
                rm1.rollback(xid1);
                rm2.rollback(xid2);
                rm3.rollback(xid3);
                log.info("End to rollback RMs.");
            }

        } catch (SQLException | XAException e) {
            // 如果出现异常，也要进行回滚
            log.warn("Rollback Exception.", e);
            try {
                if (xid1 != null) {
                    rm1.rollback(xid1);
                }
                if (xid2 != null) {
                    rm2.rollback(xid2);
                }
                if (xid3 != null) {
                    rm3.rollback(xid3);
                }
            } catch (XAException ex) {
                log.warn("Unexpected Error.", ex);
            }
        }
    }

    private static void createDataBase(String connectionUrl, String dbName) throws SQLException {
        try (Connection conn = DriverManager.getConnection(connectionUrl, USER, PASS)) {
            try (Statement st = conn.createStatement()) {
                st.execute("CREATE DATABASE IF NOT EXISTS " + dbName + ";");
            }
        }

        try (Connection conn = DriverManager.getConnection(connectionUrl + dbName, USER, PASS)) {
            try (Statement st = conn.createStatement()) {
                st.execute("DROP TABLE IF EXISTS `user`;");
                st.execute("CREATE TABLE `user`(`name` VARCHAR(16) NOT NULL);");
            }
        }
    }

    private static String getName(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT `name` FROM user;")) {
            if (rs.next()) {
                return rs.getString("name");
            } else {
                return null;
            }
        }
    }

    private static Xid createXid(String branchId) {
        return new MariaDbXid(FORMAT_ID, GTRID, branchId.getBytes());
    }

    private static void executeBranch(XAResource rm, Xid xid, Connection conn, String sql) throws XAException, SQLException {
        rm.start(xid, XAResource.TMNOFLAGS);//One of TMNOFLAGS, TMJOIN, or TMRESUME.
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.execute();
        }
        rm.end(xid, XAResource.TMSUCCESS);
    }
}
