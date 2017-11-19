package com.example.bot.spring.model;

import com.example.bot.spring.DatabaseException;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Dialogue {
    public final String customerId;
    public final ZonedDateTime sendTime;
    public final String content;

    public static Dialogue fromResultSet(ResultSet resultSet) throws SQLException {
        Timestamp ts = resultSet.getTimestamp(2);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(ts.toInstant(), ZoneOffset.UTC);
        return new Dialogue(resultSet.getString(1),
            zonedDateTime,
            resultSet.getString(3));
    }
}

