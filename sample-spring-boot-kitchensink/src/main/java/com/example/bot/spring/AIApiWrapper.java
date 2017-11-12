package com.example.bot.spring;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIContext;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AIApiWrapper {
    private static final String API_KEY = "c4b93edcda0747c483f499c40f0814e1";
    private AIConfiguration configuration = new AIConfiguration(API_KEY);
    private AIDataService dataService = new AIDataService(configuration);

    public Result getIntent(String text) {
        AIRequest request = new AIRequest(text);
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

    public void setContext(String context) {
        dataService.resetContexts();
        AIRequest addContextRequest = new AIRequest();
        addContextRequest.addContext(new AIContext(context));
        try {
            dataService.request(addContextRequest);
        } catch (AIServiceException e) {
            e.printStackTrace();
        }
    }
}
