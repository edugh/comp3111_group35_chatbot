package com.example.bot.spring;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.*;
import com.linecorp.bot.model.event.source.Source;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class AIApiWrapper {
    private static final String API_KEY = "c4b93edcda0747c483f499c40f0814e1";

    public static AIContext createInputContext(Pair<String, Integer> pair) {
        AIContext context = new AIContext(pair.getLeft());
        context.setLifespan(pair.getRight());
        return context;
    }

    public static List<AIContext> createInputContexts(List<Pair<String, Integer>> contexts) {
        return contexts.stream().map(AIApiWrapper::createInputContext).collect(Collectors.toList());
    }

    public static Set<String> getOutputContexts(List<AIOutputContext> contexts) {
        return contexts.stream().map(AIOutputContext::getName).collect(Collectors.toSet());
    }

    public static Result sendRequest(AIRequest request, Source source) {
        AIConfiguration configuration = new AIConfiguration(API_KEY);
        AIDataService dataService = new AIDataService(configuration, source::getUserId);
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

    public static Result getIntent(String text, Source source, List<Pair<String, Integer>> contexts) {
        AIRequest request = new AIRequest(text);
        request.setSessionId(source.getUserId());
        request.setContexts(createInputContexts(contexts));
        return sendRequest(request, source);
    }

    public static void resetContexts(Source source) {
        AIConfiguration configuration = new AIConfiguration(API_KEY);
        AIDataService dataService = new AIDataService(configuration, source::getUserId);

        dataService.resetContexts();
    }

    public static Result setContext(List<Pair<String, Integer>> contexts, Source source) {
        final AIRequest cleanRequest = new AIRequest();
        cleanRequest.setQuery("empty_query_for_resetting_contexts");
        cleanRequest.setContexts(createInputContexts(contexts));
        cleanRequest.setSessionId(source.getUserId());
        return sendRequest(cleanRequest, source);
    }
}
