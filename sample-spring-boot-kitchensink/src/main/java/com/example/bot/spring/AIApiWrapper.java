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

/**
 * AIApiWrapper is our interface to DialogFlow. It exposes methods to get Intent from the users query,
 * and set or reset context.
 */
@Slf4j
public class AIApiWrapper {
    private static final String API_KEY = "c4b93edcda0747c483f499c40f0814e1";

    /**
     * Creates context from name and lifespan
     * @param pair pair of name and lifespan of the intended context
     * @return the created context
     */
    private static AIContext createInputContext(Pair<String, Integer> pair) {
        AIContext context = new AIContext(pair.getLeft());
        context.setLifespan(pair.getRight());
        return context;
    }

    /**
     * Maps a list of pairs to contexts through createInputContext
     * @param contexts context pairs to convert to contexts
     * @return the list of contexts created
     */
    private static List<AIContext> createInputContexts(List<Pair<String, Integer>> contexts) {
        return contexts.stream().map(AIApiWrapper::createInputContext).collect(Collectors.toList());
    }

    /**
     * Send the given request through the users channel and return the resulting intent
     * @param request The request to send
     * @param source User to send the request to the proper dialogflow chanel
     * @return Resolved intent
     */
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

    /**
     * Gets an intent for a given users query, if contexts are sent set them for the user
     * @param text User query
     * @param source User to send query to
     * @param contexts Contexts to set for user
     * @return The resulting intent
     */
    public static Result getIntent(String text, Source source, List<Pair<String, Integer>> contexts) {
        AIRequest request = new AIRequest(text);
        request.setSessionId(source.getUserId());
        request.setContexts(createInputContexts(contexts));
        return sendRequest(request, source);
    }

    /**
     * Resets all contexts for user
     * @param source User to reset contexts for
     */
    public static void resetContexts(Source source) {
        AIConfiguration configuration = new AIConfiguration(API_KEY);
        AIDataService dataService = new AIDataService(configuration, source::getUserId);

        dataService.resetContexts();
    }

    /**
     * Sets contexts for the user
     * @param contexts new contexts to set
     * @param source user to set contexts for
     * @return Resulting Intent with new contexts
     */
    public static Result setContext(List<Pair<String, Integer>> contexts, Source source) {
        final AIRequest cleanRequest = new AIRequest();
        cleanRequest.setQuery("empty_query_for_resetting_contexts");
        cleanRequest.setContexts(createInputContexts(contexts));
        cleanRequest.setSessionId(source.getUserId());
        return sendRequest(cleanRequest, source);
    }
}
