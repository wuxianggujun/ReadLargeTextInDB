package org.wuxianggujun.util;

import java.sql.*;

/**
 * @author WuXiangGuJun
 * @create 2023-01-15 12:55
 **/
public class JDBCUtils {
    public static final String JDBC_URL = "jdbc:mysql://localhost:3306/SGKTest?rewriteBatchedStatements=true";
    public static final String JDBC_USER = "root";
    public static final String JDBC_PASSWORD = "root";
    public static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    static {
        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static boolean execute(String sql, Object... param) {
        boolean result = true;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            //开启事务
            connection = getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(sql);
            for (int i = 0; i < param.length; i++) {
                preparedStatement.setObject(i, param[i]);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            //提交事务
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    // 日志记录事务回滚失败
                    return result = false;
                }
            }
            result = false;
        } finally {
            close(preparedStatement, connection);
        }
        return result;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }


    public static void close(Statement statement, Connection connection, ResultSet resultSet) {
        close(statement, connection);
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(Statement statement, Connection connection) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws SQLException {
        String str = "3523406341----17730321049";

        var start = System.currentTimeMillis();

        for (int i = 0; i < 1000000; i++) {
            String qq = str.substring(0, str.indexOf("-"));
            String phone = str.substring(str.lastIndexOf('-') + 1);
        }
        System.out.println("System.currentTimeMillis() = " + (System.currentTimeMillis()-start));

        var start2 = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            String[] split = str.split("----");
            String qq = split[0];
            String phone = split[1];
        }
        System.out.println("Cunt 2 time  = " + (System.currentTimeMillis()-start2));

    }
}
