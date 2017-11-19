package com.example.bot.spring;
import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface SQLModelReader<T> {
    T apply(ResultSet t) throws SQLException;
}
