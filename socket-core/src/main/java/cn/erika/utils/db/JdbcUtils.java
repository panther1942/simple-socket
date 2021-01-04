package cn.erika.utils.db;

import cn.erika.utils.log.Logger;
import cn.erika.utils.log.LoggerFactory;
import cn.erika.utils.string.StringUtils;

import java.sql.*;
import java.util.*;

public class JdbcUtils {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private static JdbcUtils utils = new JdbcUtils();

    // 自己写个简单的连接池
    private Vector<Connection> freePool = new Vector<>();
    private Vector<Connection> usedPool = new Vector<>();

    private static String[] driverList = {
            "com.mysql.jdbc.Driver",
            "com.mysql.cj.jdbc.Driver",
            "org.sqlite.JDBC"
    };

    static {
        for (String driver : driverList) {
            try {
                loadDriver(driver);
            } catch (ClassNotFoundException e) {
                System.err.println("忽略驱动: " + driver);
            }
        }
    }

    private static void loadDriver(String name) throws ClassNotFoundException {
        Class.forName(name);
    }

    public static JdbcUtils getInstance(){
        if (utils == null) {
            utils = new JdbcUtils();
        }
        return utils;
    }

    private JdbcUtils(){}

    public Connection getConn(String url, String username, String password) throws SQLException {
        Connection conn;
        if (freePool.size() > 0) {
            conn = freePool.lastElement();
            freePool.remove(conn);
            usedPool.add(conn);
        } else {
            conn = DriverManager.getConnection(url, username, password);
            usedPool.add(conn);
        }
        return conn;
    }

    public Connection getConn() throws SQLException {
        String url = "jdbc:sqlite:/home/erika/Workspaces/simple-socket/localStorage.db";
        String username = "";
        String password = "";
        return getConn(url, username, password);
    }

    public List<Map<String, Object>> select(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet result = null;
        try{
            conn = getConn();
            pStmt = conn.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    pStmt.setObject(i + 1, params[i]);
                }
            }
            result = pStmt.executeQuery();
            ResultSetMetaData meta = result.getMetaData();
            List<Map<String,Object>> resultList = new LinkedList<>();
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

    public int update(String sql, Object... params) {
        log.debug(sql);
        if (params != null) {
            log.debug(StringUtils.join(",", params).toString());
        }
        Connection conn = null;
        PreparedStatement pStmt = null;
        try {
            conn = getConn();
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
