package com.example.bot.spring;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Utility class to allow exceptions to be thrown when serializing our models
 * @param <T>
 */
@FunctionalInterface
public interface SQLModelReader<T> {
    T apply(ResultSet t) throws SQLException;
}
