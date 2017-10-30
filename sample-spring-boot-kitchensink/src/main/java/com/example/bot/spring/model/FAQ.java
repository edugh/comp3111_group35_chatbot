package com.example.bot.spring.model;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FAQ {
    public final String question;
    public final String answer;

    public FAQ(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public static FAQ fromResultSet(ResultSet resultSet) throws SQLException {
        return new FAQ(resultSet.getString(1), resultSet.getString(2));
    }
}
