package com.example.bot.spring.model;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Tour {
    public final String id;
    public final String name;
    public final String shortDescription;
    public final int length;
    public final String departure;
    public final String price;

    public Tour(String id,
                String name,
                String shortDescription,
                int length,
                String departure,
                String price) {
        this.id = id;
        this.name = name;
        this.shortDescription = shortDescription;
        this.length = length;
        this.departure = departure;
        this.price = price;
    }

    public static Tour fromResultSet(ResultSet resultSet) throws SQLException {
        return new Tour(resultSet.getString(1),
                resultSet.getString(2),
                resultSet.getString(3),
                resultSet.getInt(4),
                resultSet.getString(5),
                resultSet.getString(6));
    }
}
