package cn.erika.utils.db;

import cn.erika.config.GlobalSettings;
import cn.erika.utils.log.Logger;
import cn.erika.utils.log.LoggerFactory;
import cn.erika.utils.string.StringUtils;

import java.sql.*;
import java.util.*;

public class JdbcUtils {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private static JdbcUtils utils;

    private String driver = GlobalSettings.dbDriver;
    private String url = GlobalSettings.dbUrl;
    private String username = GlobalSettings.dbUsername;
    private String password = GlobalSettings.dbPassword;
    private String testSql = GlobalSettings.dbTestSql;

    // 自己写个简单的连接池
    private Vector<Connection> freePool = new Vector<>();
    private Vector<Connection> usedPool = new Vector<>();
    private int maxConn = 10;
    private int waitTime = 5 * 1000;

    private void loadDriver(String name) throws ClassNotFoundException {
        Class.forName(name);
    }

    public static JdbcUtils getInstance() throws SQLException {
        if (utils == null) {
            utils = new JdbcUtils();
        }
        return utils;
    }

    private JdbcUtils() throws SQLException {
        try {
            loadDriver(driver);
        } catch (ClassNotFoundException e) {
            log.error("找不到指定的驱动: " + driver);
            System.exit(1);
        }
        if (select(testSql).size() > 0) {
            log.info("数据库连接成功");
        } else {
            log.warn("无法连接到数据库");
        }
    }

    private synchronized Connection getNewConn(String url, String username, String password) throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public Connection getConn(String url, String username, String password) throws SQLException {
        Connection conn = null;
        try {
            while (conn == null) {
                if (freePool.size() > 0) {
                    conn = freePool.lastElement();
                    freePool.remove(conn);
                    usedPool.add(conn);
                } else if (usedPool.size() < maxConn) {
                    conn = getNewConn(url, username, password);
                    usedPool.add(conn);
                } else {
                    Thread.sleep(waitTime);
                }
            }
            return conn;
        } catch (InterruptedException e) {
            throw new SQLException("无法获取数据库连接");
        }
    }

    public Connection getConn() throws SQLException {
        return getConn(url, username, password);
    }

    public List<Map<String, Object>> select(Connection conn, String sql, Object... params) throws SQLException {
        PreparedStatement pStmt = null;
        ResultSet result = null;
        try {
            pStmt = conn.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    pStmt.setObject(i + 1, params[i]);
                }
            }
            result = pStmt.executeQuery();
            ResultSetMetaData meta = result.getMetaData();
            List<Map<String, Object>> resultList = new LinkedList<>();
            int columnCount = meta.getColumnCount();
            String[] columnNames = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                columnNames[i] = meta.getColumnName(i + 1);
            }
            while (result.next()) {
                Map<String, Object> entry = new HashMap<>();
                for (int i = 0; i < columnCount; i++) {
                    entry.put(columnNames[i], result.getObject(i + 1));
                }
                resultList.add(entry);
            }
            return resultList;
        } finally {
            close(conn, pStmt, result);
        }
    }

    public List<Map<String, Object>> select(String sql, Object... params) throws SQLException {
        Connection conn = getConn();
        return select(conn, sql, params);
    }

    public int update(Connection conn, String sql, Object... params) throws SQLException {
        PreparedStatement pStmt = null;
        try {
            pStmt = conn.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    pStmt.setObject(i + 1, params[i]);
                }
            }
            return pStmt.executeUpdate();
        } finally {
            close(conn, pStmt);
        }
    }

    public int update(String sql, Object... params) throws SQLException {
        Connection conn = getConn();
        return update(conn, sql, params);
    }

    public void close(Connection conn, Statement stmt) {
        close(conn, stmt, null);
    }

    public void close(Connection conn, Statement stmt, ResultSet result) {
        try {
            if (result != null) {
                result.close();
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        if (conn != null) {
            closeConnection(conn);
        }
    }

    private void closeConnection(Connection conn) {
        if (conn != null) {
            usedPool.remove(conn);
            freePool.add(conn);
        }
    }
}
