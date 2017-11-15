package com.example.bot.spring;

import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.net.URISyntaxException;
import java.sql.*;

@Slf4j
public class MockDatabaseEngine extends DatabaseEngine {

    private DataSource dataSource;

    public MockDatabaseEngine(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}
