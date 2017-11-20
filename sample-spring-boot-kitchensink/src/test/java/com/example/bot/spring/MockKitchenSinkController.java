package com.example.bot.spring;

import com.linecorp.bot.model.message.Message;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Mock of KitchenSinkController that stores messages for easier testing
 */
public class MockKitchenSinkController extends KitchenSinkController {
    private List<Message> messages = new ArrayList<>();

    public MockKitchenSinkController(DatabaseEngine databaseEngine) {
        super(databaseEngine);
    }

    public void clearMessages() {
        messages.clear();
    }

    public List<Message> getLatestMessages() {
        List<Message> oldMessages = new ArrayList<>(this.messages);
        messages.clear();
        return oldMessages;
    }

    @Override
    protected void reply(@NonNull String replyToken, @NonNull List<Message> messages) {
        this.messages.addAll(messages);
    }

    @Override
    protected void push(@NonNull Set<String> userIds, @NonNull Message message) {

    }

    @Override
    protected void push(@NonNull String userId, @NonNull Message message) {

    }
}
