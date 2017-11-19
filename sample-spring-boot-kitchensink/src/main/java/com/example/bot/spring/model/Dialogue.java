package com.example.bot.spring.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Dialogue {
    public final String customerId;
    public final Timestamp sendTime;
    public final String content;

    public static Dialogue fromResultSet(ResultSet resultSet) throws SQLException {
        return new Dialogue(resultSet.getString(1),
                resultSet.getTimestamp(2),
                resultSet.getString(3));
    }
}

