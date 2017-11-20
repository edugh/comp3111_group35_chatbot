package com.example.bot.spring.model;

import com.example.bot.spring.DatabaseException;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Pojo representing a Plan row in the database */
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public final class Plan {
    public final String id;
    public final String name;
    public final String shortDescription;
    public final int length;
    public final String departure;
    public final BigDecimal weekdayPrice;
    public final BigDecimal weekendPrice;

    public static Plan fromResultSet(ResultSet resultSet) throws SQLException {
        return new Plan(resultSet.getString(1),
            resultSet.getString(2),
            resultSet.getString(3),
            resultSet.getInt(4),
            resultSet.getString(5),
            resultSet.getBigDecimal(6),
            resultSet.getBigDecimal(7));
    }
}
