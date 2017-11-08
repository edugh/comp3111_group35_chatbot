package com.example.bot.spring;

import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;

import java.net.URISyntaxException;
import java.sql.*;

@Slf4j
public class MockDatabaseEngine extends DatabaseEngine {

    private DSLContext create;

    public MockDatabaseEngine(DSLContext create) {
        this.create = create;
    }

    public Connection getConnection() throws URISyntaxException, SQLException {
        MockDataProvider provider = new MyProvider();
        MockConnection connection = new MockConnection(provider);

        // Pass the mock connection to a jOOQ DSLContext:
        DSLContext create = DSL.using(connection, SQLDialect.ORACLE);

        // Execute queries transparently, with the above DSLContext:
        Result<BookRecord> result = create.selectFrom(BOOK).where(BOOK.ID.eq(5)).fetch();
    }
}
