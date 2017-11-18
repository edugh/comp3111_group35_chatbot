package com.example.bot.spring.model;

import com.example.bot.spring.DatabaseException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class Dialogue {
    public final String customerId;
    public final ZonedDateTime sendTime;
    public final String content;

    public Dialogue(
            String customerId,
            ZonedDateTime sendTime,
            String content
    ) {
        this.customerId = customerId;
        this.sendTime = sendTime;
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Dialogue dialogue = (Dialogue) o;

        if (customerId != null ? !customerId.equals(dialogue.customerId) : dialogue.customerId != null) return false;
        if (sendTime != null ? !sendTime.equals(dialogue.sendTime) : dialogue.sendTime != null) return false;
        return content != null ? content.equals(dialogue.content) : dialogue.content == null;
    }

    @Override
    public int hashCode() {
        int result = customerId != null ? customerId.hashCode() : 0;
        result = 31 * result + (sendTime != null ? sendTime.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }

    public String toString() {
        return "Dialogue{" +
                "customerId='" + customerId + '\'' +
                ", sendTime=" + sendTime +
                ", content='" + content + '\'' +
                '}';
    }

    public static Dialogue fromResultSet(ResultSet resultSet) {
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
}

