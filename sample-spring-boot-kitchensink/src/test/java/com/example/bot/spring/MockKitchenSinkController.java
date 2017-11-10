package com.example.bot.spring;

import com.linecorp.bot.model.message.Message;
import lombok.NonNull;

import java.util.List;

public class MockKitchenSinkController extends KitchenSinkController {
    private List<Message> messages;

    public MockKitchenSinkController(DatabaseEngine databaseEngine) {
        super(databaseEngine);
    }

    public List<Message> getLatestMessages() {
        return messages;
    }

    @Override
    protected void reply(@NonNull String replyToken, @NonNull List<Message> messages) {
        this.messages = messages;
    }
}
