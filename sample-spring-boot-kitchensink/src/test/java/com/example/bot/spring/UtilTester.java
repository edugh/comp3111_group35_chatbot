package com.example.bot.spring;

import com.example.bot.spring.model.Plan;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

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

    @Test
    public void testGetWeekdayMethods() {
        String tuesday = "Tue";
        String wednesday = "Wed";
        Date thursday = new Date(0);
        Date friday = new Date(86400000);
        Assert.assertEquals(Utils.getDateOfWeek(tuesday), 3);
        Assert.assertEquals(Utils.getDateOfWeek(wednesday), 4);
        Assert.assertEquals(Utils.getDateOfWeek(thursday), 5);
        Assert.assertEquals(Utils.getDateOfWeek(friday), 6);
    }

    @Test
    public void testRatePlans() {
        Date thursday = new Date(0);
        Date friday = new Date(86400000);
        Date saturday = new Date(86400000 * 2);
        Plan plan = new Plan("Id1", "Shimen National Forest Tour", "Nanhua Temple", 0, "Thu, Fri", null, null);
        Assert.assertEquals(Utils.ratePlanForCriteria(thursday, null, plan), 100);
        Assert.assertEquals(Utils.ratePlanForCriteria(friday, null, plan), 100);
        Assert.assertEquals(Utils.ratePlanForCriteria(saturday, null, plan), 60);
        Assert.assertEquals(Utils.ratePlanForCriteria(null, "Shimen National", plan), 100);
        Assert.assertEquals(Utils.ratePlanForCriteria(null, "Shimen Nanhua", plan), 100);
        Assert.assertEquals(Utils.ratePlanForCriteria(saturday, "Other Place", plan), 0);
        Assert.assertEquals(Utils.ratePlanForCriteria(friday, "Shimen and Other", plan), 60);
    }

    public <T> void assertIteratorEquals(Iterator<T> first, Iterator<T> second) {
        while (first.hasNext() && second.hasNext()) {
            T firstElement = first.next();
            T secondElement = second.next();
            System.out.println(firstElement.toString());
            System.out.println(secondElement.toString());
            Assert.assertEquals(firstElement, secondElement);
        }
        Assert.assertFalse(first.hasNext());
        Assert.assertFalse(second.hasNext());
    }

    @Test
    public void testFilterAndSortTourResults() {
        Date thursday = new Date(0);
        Date friday = new Date(86400000);
        Plan plan1 = new Plan("Id1", "Shimen National Forest Tour", "", 0, "Thu", null, null);
        Plan plan2 = new Plan("Id2", "Yangshan Hot Spring Tour", "", 0, "Thu, Fri", null, null);
        Plan plan3 = new Plan("Id3", "Shaoguan sight-seeing tour", "Nanhua Temple", 0, "Fri", null, null);
        Plan plan4 = new Plan("Id3", "National Park Tour", "Also Nanhua Temple", 0, "Thu", null, null);
        List<Plan> plans = Arrays.asList(plan1, plan2, plan3, plan4);

        Iterator<Plan> expectedPlans = Arrays.asList(plan3, plan4).iterator();
        assertIteratorEquals(Utils.filterAndSortTourResults(null, "Nanhua", plans), expectedPlans);

        expectedPlans = Arrays.asList(plan1, plan4).iterator();
        assertIteratorEquals(Utils.filterAndSortTourResults(null, "Shimen National", plans), expectedPlans);

        expectedPlans = Arrays.asList(plan1, plan2, plan4, plan3).iterator();
        assertIteratorEquals(Utils.filterAndSortTourResults(thursday, null, plans), expectedPlans);

        expectedPlans = Arrays.asList(plan2, plan3, plan1, plan4).iterator();
        assertIteratorEquals(Utils.filterAndSortTourResults(friday, null, plans), expectedPlans);

        expectedPlans = Arrays.asList(plan4, plan3).iterator();
        assertIteratorEquals(Utils.filterAndSortTourResults(thursday, "Nanhua", plans), expectedPlans);

        expectedPlans = Arrays.asList(plan3).iterator();
        assertIteratorEquals(Utils.filterAndSortTourResults(friday, "Shaoguan", plans), expectedPlans);

        expectedPlans = Arrays.asList(plan2).iterator();
        assertIteratorEquals(Utils.filterAndSortTourResults(thursday, "Yangshan", plans), expectedPlans);
    }
}
