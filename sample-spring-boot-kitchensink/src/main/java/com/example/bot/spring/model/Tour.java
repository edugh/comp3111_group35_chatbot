package com.example.bot.spring.model;

import com.example.bot.spring.DatabaseException;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Tour {
    public String planId;
    public Date tourDate;
    public String guideName;
    public String guideAccount;
    public String hotel;
    public int capacity;
    public int minimum;

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
