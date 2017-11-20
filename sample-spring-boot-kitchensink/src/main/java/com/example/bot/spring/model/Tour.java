package com.example.bot.spring.model;

import com.example.bot.spring.DatabaseException;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Pojo representing a Tour row in the database */
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public final class Tour {
    public final String planId;
    public final Date tourDate;
    public final String guideName;
    public final String guideAccount;
    public final String hotel;
    public final int capacity;
    public final int minimum;

    public static Tour fromResultSet(ResultSet resultSet) throws SQLException {
        return new Tour(
            resultSet.getString(1),
            resultSet.getDate(2),
            resultSet.getString(3),
            resultSet.getString(4),
            resultSet.getString(5),
            resultSet.getInt(6),
            resultSet.getInt(7));
    }
}
