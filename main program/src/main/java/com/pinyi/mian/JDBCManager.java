package com.pinyi.mian;

import java.sql.*;


public class JDBCManager {
    public static String url;
    public static String username;
    public static String password;
    public static final String driver = "com.mysql.cj.jdbc.Driver";

    static {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("加载数据库驱动失败");
        }
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new RuntimeException("建立数据库连接失败");
        }
    }

    public static void closeConnection(ResultSet resultSet, PreparedStatement preparedStatement, Connection connection) {
        try {
            if (resultSet != null) resultSet.close();

            if (preparedStatement != null) preparedStatement.close();

            if (connection != null) connection.close();
        } catch (SQLException e) {
            throw new RuntimeException("建立数据库连接失败");
        }
    }
}
