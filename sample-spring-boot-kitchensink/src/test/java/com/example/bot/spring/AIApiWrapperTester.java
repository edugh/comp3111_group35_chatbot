package com.example.bot.spring;

import ai.api.model.Result;
import com.google.common.collect.Sets;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.event.source.UserSource;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class AIApiWrapperTester {

    private Source source;

    @Before
    public void beforeEachTest() throws Exception {
        source = new UserSource("ApiWrapperTestSource");
        AIApiWrapper.resetContexts(source);
    }

    @Test
    public void testNormalQuery() {
        Result result = AIApiWrapper.getIntent("How much do I owe", source, Collections.EMPTY_LIST);
        Assert.assertEquals(result.getContexts(), Collections.EMPTY_LIST);
        Assert.assertEquals(result.getMetadata().getIntentName(), "AmountOwed");
    }

    @Test
    public void testFAQ() {
        Result result = AIApiWrapper.getIntent("How to apply?", source, Collections.EMPTY_LIST);
        Assert.assertEquals(result.getContexts(), Collections.EMPTY_LIST);
        Assert.assertEquals(result.getMetadata().getIntentName(), "FAQ1");
    }

    @Test
    public void testGivePeopleFlow() {
        Result result = AIApiWrapper.getIntent("3", source, Arrays.asList(new ImmutablePair<>("NeedAdults", 10)));
        Assert.assertEquals(result.getMetadata().getIntentName(), "GiveAdults");
        Assert.assertEquals(result.getIntParameter("number-integer"), 3);
        Set<String> contexts = AIApiWrapper.getOutputContexts(result.getContexts());
        Assert.assertEquals(contexts, Sets.newHashSet("needchildren"));

        result = AIApiWrapper.getIntent("2", source, Collections.EMPTY_LIST);
        Assert.assertEquals(result.getMetadata().getIntentName(), "GiveChildren");
        Assert.assertEquals(result.getIntParameter("number-integer"), 2);
        contexts = AIApiWrapper.getOutputContexts(result.getContexts());
        Assert.assertEquals(contexts,Sets.newHashSet("needtoddlers"));
    }

    @Test
    public void testModifyContextThroughSDK() {
        Result result = AIApiWrapper.setContext(Arrays.asList(
                new ImmutablePair<>("NeedAdults", 10),
                new ImmutablePair<>("NeedDeparture", 10)
        ), source);
        Set<String> contexts = AIApiWrapper.getOutputContexts(result.getContexts());
        Assert.assertEquals(contexts, Sets.newHashSet("needadults", "needdeparture"));

        result = AIApiWrapper.setContext(Arrays.asList(
                new ImmutablePair<>("NeedAdults", 0),
                new ImmutablePair<>("NeedChildren", 10)
        ), source);
        contexts = AIApiWrapper.getOutputContexts(result.getContexts());
        Assert.assertEquals(contexts, Sets.newHashSet("needchildren", "needdeparture"));
    }
}
