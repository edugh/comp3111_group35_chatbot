package com.example.bot.spring.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;

public class Plan {
    public final String id;
    public final String name;
    public final String shortDescription;
    public final int length;
    public final String departure;
    public final BigDecimal price;

    public Plan(String id,
                String name,
                String shortDescription,
                int length,
                String departure,
                BigDecimal price) {
        this.id = id;
        this.name = name;
        this.shortDescription = shortDescription;
        this.length = length;
        this.departure = departure;
        this.price = price;
    }

    public static Plan fromResultSet(ResultSet resultSet) throws SQLException {
        return new Plan(resultSet.getString(1),
                resultSet.getString(2),
                resultSet.getString(3),
                resultSet.getInt(4),
                resultSet.getString(5),
                new BigDecimal(resultSet.getString(6)));
    }
}
