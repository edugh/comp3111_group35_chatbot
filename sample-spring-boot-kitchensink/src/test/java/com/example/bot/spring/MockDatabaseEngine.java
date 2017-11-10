package com.example.bot.spring;

import lombok.extern.slf4j.Slf4j;
import java.net.URISyntaxException;
import java.sql.*;

@Slf4j
public class MockDatabaseEngine extends DatabaseEngine {

    private Connection connection;

    public MockDatabaseEngine(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() throws URISyntaxException, SQLException {
        return connection;
    }
}
