package com.example.bot.spring;

import com.example.bot.spring.model.*;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;

@Slf4j
public class MockDatabaseEngine extends SQLDatabaseEngine {

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
