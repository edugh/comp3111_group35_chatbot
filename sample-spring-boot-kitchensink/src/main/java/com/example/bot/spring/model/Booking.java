package com.example.bot.spring.model;

import com.example.bot.spring.DatabaseException;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Pojo representing a Booking row in the database */
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public final class Booking {
    public final String customerId;
    public final String planId;
    public final Date tourDate;
    public final Integer adults;
    public final Integer children;
    public final Integer toddlers;
    public final BigDecimal fee;
    public final BigDecimal paid;
    public final String specialRequest;

    public static Booking fromResultSet(ResultSet resultSet) throws SQLException {
        return new Booking(resultSet.getString(1),
                resultSet.getString(2),
                resultSet.getDate(3),
                resultSet.getInt(4),
                resultSet.getInt(5),
                resultSet.getInt(6),
                resultSet.getBigDecimal(7),
                resultSet.getBigDecimal(8),
                resultSet.getString(9)
        );
    }
}
