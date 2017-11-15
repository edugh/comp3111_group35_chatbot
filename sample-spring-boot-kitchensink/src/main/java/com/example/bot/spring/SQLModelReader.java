package com.example.bot.spring;
import java.sql.ResultSet;

@FunctionalInterface
public interface SQLModelReader<T> {
    T apply(ResultSet t);
}
