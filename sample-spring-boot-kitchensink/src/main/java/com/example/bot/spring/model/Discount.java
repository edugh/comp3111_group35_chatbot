package com.example.bot.spring.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Discount {
    public final String customerId;
    public final String planId;
    public final Date tourDate;
    public final Integer seats;

    public static Discount fromResultSet(ResultSet resultSet) throws SQLException {
        return new Discount(
                resultSet.getString(1),
                resultSet.getString(2),
                resultSet.getDate(3),
                resultSet.getInt(4)
        );
    }
}
