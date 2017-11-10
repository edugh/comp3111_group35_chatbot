package com.example.bot.spring;

import org.junit.Assert;
import org.junit.Test;

public class UtilTester {

    @Test
    public void testFuzzyMatchPositive() {
        Assert.assertTrue(Utils.stupidFuzzyMatch("a b c d", "d c d a"));
        Assert.assertTrue(Utils.stupidFuzzyMatch("a b c d", "d c b"));
        Assert.assertTrue(Utils.stupidFuzzyMatch("b c d", "d c b a"));
    }

    @Test
    public void testFuzzyMatchNegative() {
        Assert.assertFalse(Utils.stupidFuzzyMatch("a b c d", "e f g h"));
        Assert.assertFalse(Utils.stupidFuzzyMatch("a b c d", "c d e f"));
    }
}
