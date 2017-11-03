package com.example.bot.spring.model;

public class FAQ {
    public final String question;
    public final String answer;

    public FAQ(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FAQ faq = (FAQ) o;

        if (question != null ? !question.equals(faq.question) : faq.question != null) return false;
        return answer != null ? answer.equals(faq.answer) : faq.answer == null;
    }

    @Override
    public int hashCode() {
        int result = question != null ? question.hashCode() : 0;
        result = 31 * result + (answer != null ? answer.hashCode() : 0);
        return result;

    public String toString() {
        return "FAQ{" +
                "question='" + question + '\'' +
                ", answer='" + answer + '\'' +
                '}';
    }
}
