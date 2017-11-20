package com.example.bot.spring.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public final class DiscountSchedule {
    public final String planId;
    public final Date tourDate;
    public final Timestamp sendTime;

    public static DiscountSchedule fromResultSet(ResultSet resultSet) throws SQLException {
        return new DiscountSchedule(
                resultSet.getString(1),
                resultSet.getDate(2),
                resultSet.getTimestamp(3)
        );
    }
}
