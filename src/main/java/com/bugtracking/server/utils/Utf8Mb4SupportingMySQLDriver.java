package com.bugtracking.server.utils;

import com.mysql.jdbc.NonRegisteringDriver;

import java.sql.Connection;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;

public class Utf8Mb4SupportingMySQLDriver extends NonRegisteringDriver implements java.sql.Driver {

    private static final String ORIGINAL_MYSQL_URL_PREFIX = "jdbc:mysql:";
    private static final String URL_PREFIX = "jdbc:utf8mb4-mysql:";
    private static final int URL_PREFIX_LENGTH = URL_PREFIX.length();

    static {
        try {
            java.sql.DriverManager.registerDriver(new Utf8Mb4SupportingMySQLDriver());
        } catch (SQLException e) {
            throw new RuntimeException("Can't register driver!");
        }
    }

    public Utf8Mb4SupportingMySQLDriver() throws SQLException {
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        Connection connection = super.connect(replaceUrl(url), info);
        if (connection != null) {
            try (Statement statement = connection.createStatement()) {
                statement.addBatch("SET SESSION character_set_client = utf8mb4");
                statement.addBatch("SET SESSION character_set_results = utf8mb4");
                statement.addBatch("SET SESSION character_set_connection = utf8mb4");
                statement.addBatch("SET SESSION collation_connection = utf8mb4_unicode_ci");
                statement.executeBatch();
            }
        }
        return connection;
    }

    private static String replaceUrl(String url) {
        if (url != null && url.startsWith(URL_PREFIX)) {
            return ORIGINAL_MYSQL_URL_PREFIX + url.substring(URL_PREFIX_LENGTH);
        }
        return url;
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        if (url != null && url.startsWith(URL_PREFIX)) {
            return super.acceptsURL(replaceUrl(url));
        }
        return false;
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return super.getPropertyInfo(replaceUrl(url), info);
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("Not implemented in parent class");
    }
}
