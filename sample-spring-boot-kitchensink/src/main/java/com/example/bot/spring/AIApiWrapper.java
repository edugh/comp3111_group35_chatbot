package com.example.bot.spring;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import com.linecorp.bot.model.event.source.Source;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AIApiWrapper {
    private static final String API_KEY = "c4b93edcda0747c483f499c40f0814e1";

    public static Result getIntent(String text, Source source) {
        AIRequest request = new AIRequest(text);
        AIConfiguration configuration = new AIConfiguration(API_KEY);
        AIDataService dataService = new AIDataService(configuration, source::getUserId);
        request.setSessionId(source.getUserId());
        try {
            AIResponse response = dataService.request(request);
            if (response.getStatus().getCode() == 200) {
                return response.getResult();
            } else {
                log.error(response.getStatus().getErrorDetails());
                return null;
            }
        } catch (AIServiceException e) {
            e.printStackTrace();
            return null;
        }
    }
}
