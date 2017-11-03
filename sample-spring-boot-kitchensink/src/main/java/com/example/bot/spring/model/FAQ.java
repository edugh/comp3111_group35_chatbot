package com.example.bot.spring.model;

public class FAQ {
    public final String question;
    public final String answer;

    public FAQ(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    @Override
    public String toString() {
        return "FAQ{" +
                "question='" + question + '\'' +
                ", answer='" + answer + '\'' +
                '}';
    }
}
