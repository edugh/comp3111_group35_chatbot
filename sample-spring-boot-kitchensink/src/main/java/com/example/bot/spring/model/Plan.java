package com.example.bot.spring.model;

import com.example.bot.spring.DatabaseException;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Plan {
    public final String id;
    public final String name;
    public final String shortDescription;
    public final int length;
    public final String departure;
    public final BigDecimal weekdayPrice;
    public final BigDecimal weekendPrice;

    public Plan(String id,
                String name,
                String shortDescription,
                int length,
                String departure,
                BigDecimal price1,
                BigDecimal price2) {
        this.id = id;
        this.name = name;
        this.shortDescription = shortDescription;
        this.length = length;
        this.departure = departure;
        this.weekdayPrice = price1;
        this.weekendPrice = price2;
    }

    @Override
    public String toString() {
        return "Plan{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", shortDescription='" + shortDescription + '\'' +
                ", length=" + length +
                ", departure='" + departure + '\'' +
                ", weekdayPrice=" + weekdayPrice +
                ", weekendPrice=" + weekendPrice +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Plan plan = (Plan) o;

        if (length != plan.length) return false;
        if (id != null ? !id.equals(plan.id) : plan.id != null) return false;
        if (name != null ? !name.equals(plan.name) : plan.name != null) return false;
        if (shortDescription != null ? !shortDescription.equals(plan.shortDescription) : plan.shortDescription != null)
            return false;
        if (departure != null ? !departure.equals(plan.departure) : plan.departure != null) return false;
        if (weekdayPrice != null ? !weekdayPrice.equals(plan.weekdayPrice) : plan.weekdayPrice != null) return false;
        return weekendPrice != null ? weekendPrice.equals(plan.weekendPrice) : plan.weekendPrice == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (shortDescription != null ? shortDescription.hashCode() : 0);
        result = 31 * result + length;
        result = 31 * result + (departure != null ? departure.hashCode() : 0);
        result = 31 * result + (weekdayPrice != null ? weekdayPrice.hashCode() : 0);
        result = 31 * result + (weekendPrice != null ? weekendPrice.hashCode() : 0);
        return result;
    }

    public static Plan fromResultSet(ResultSet resultSet) {
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
}
