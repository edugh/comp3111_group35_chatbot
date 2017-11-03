package com.example.bot.spring.model;

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
    public String toString() {
        return "Dialogue{" +
                "customerId='" + customerId + '\'' +
                ", sendTime=" + sendTime +
                ", content='" + content + '\'' +
                '}';
    }
}

