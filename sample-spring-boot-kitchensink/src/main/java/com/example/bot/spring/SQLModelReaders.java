package com.example.bot.spring;

import com.example.bot.spring.model.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class SQLModelReaders {
    public static Customer customerFromResultSet(ResultSet resultSet) {
        try {
            return new Customer(resultSet.getString(1),
                    resultSet.getString(2),
                    resultSet.getString(3),
                    resultSet.getInt(4),
                    resultSet.getString(5),
                    resultSet.getString(6));
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static Dialogue dialogueFromResultSet(ResultSet resultSet) {
        try {
            Timestamp ts = resultSet.getTimestamp(2);
            ZonedDateTime zonedDateTime =
                    ZonedDateTime.ofInstant(ts.toInstant(), ZoneOffset.UTC);
            return new Dialogue(resultSet.getString(1),
                    zonedDateTime,
                    resultSet.getString(3));
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static Tag tagFromResultSet(ResultSet resultSet) {
        try {
            return new Tag(resultSet.getString(1),
                    resultSet.getString(2));
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static FAQ faqFromResultSet(ResultSet resultSet) {
        try {
            return new FAQ(resultSet.getString(1), resultSet.getString(2));
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static Booking bookingFromResultSet(ResultSet resultSet) {
        try {
            return new Booking(resultSet.getString(1),
                    resultSet.getString(2),
                    resultSet.getDate(3),
                    resultSet.getInt(4),
                    resultSet.getInt(5),
                    resultSet.getInt(6),
                    resultSet.getBigDecimal(7),
                    resultSet.getBigDecimal(8),
                    resultSet.getString(9));
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static Plan planFromResultSet(ResultSet resultSet) {
        try {
            return new Plan(resultSet.getString(1),
                    resultSet.getString(2),
                    resultSet.getString(3),
                    resultSet.getInt(4),
                    resultSet.getString(5),
                    resultSet.getBigDecimal(6),
                    resultSet.getBigDecimal(7));
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static Tour tourFromResultSet(ResultSet resultSet) {
        try {
            return new Tour(
                    resultSet.getString(1),
                    resultSet.getDate(2),
                    resultSet.getString(3),
                    resultSet.getString(4),
                    resultSet.getString(5),
                    resultSet.getInt(6),
                    resultSet.getInt(7)

            );
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}
