package cn.erika.utils.db;

import cn.erika.utils.log.Logger;
import cn.erika.utils.log.LoggerFactory;
import cn.erika.utils.string.StringUtils;

import java.sql.*;
import java.util.*;

public class JdbcUtils {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private static JdbcUtils utils;

    private String driver;
    //    private String url = "jdbc:sqlite:/home/erika/Workspaces/simple-socket/localStorage.db";
    private String url = "jdbc:mysql://127.0.0.1:3306/db_development";
    private String username = "test";
    private String password = "test";

    // 自己写个简单的连接池
    private Vector<Connection> freePool = new Vector<>();
    private Vector<Connection> usedPool = new Vector<>();
    private int maxConn = 10;
    private int waitTime = 5 * 1000;

    private void loadDriver(String name) throws ClassNotFoundException {
        Class.forName(name);
    }

    public static JdbcUtils getInstance() {
        if (utils == null) {
            utils = new JdbcUtils();
        }
        return utils;
    }

    private JdbcUtils() {
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

    public List<Map<String, Object>> select(Connection conn, String sql, Object... params) {
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
        } catch (SQLException e) {
            log.error("数据查询出错: " + e.getMessage(), e);
        } finally {
            close(conn, pStmt, result);
        }
        return null;
    }

    public List<Map<String, Object>> select(String sql, Object... params) {
        Connection conn = null;
        try {
            conn = getConn();
            return select(conn, sql, params);
        } catch (SQLException e) {
            log.error("数据查询出错: " + e.getMessage(), e);
        }
        return null;
    }

    public int update(Connection conn, String sql, Object... params) {
        PreparedStatement pStmt = null;
        log.debug(sql);
        if (params != null) {
            log.debug(StringUtils.join(",", params).toString());
        }
        try {
            pStmt = conn.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    pStmt.setObject(i + 1, params[i]);
                }
            }
            return pStmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage());
            return 0;
        } finally {
            close(conn, pStmt);
        }
    }

    public int update(String sql, Object... params) {
        Connection conn = null;
        try {
            conn = getConn();
            return update(conn, sql, params);
        } catch (SQLException e) {
            log.error(e.getMessage());
            return 0;
        }
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
