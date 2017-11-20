package com.example.bot.spring;

import com.example.bot.spring.model.*;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.validation.constraints.AssertTrue;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.Date;

import static org.bouncycastle.asn1.ua.DSTU4145NamedCurves.params;

/**
 * Unit tests for methods defined in Utils
 */
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
        Assert.assertEquals(Utils.ratePlanForCriteria(thursday, null, null, plan), 100);
        Assert.assertEquals(Utils.ratePlanForCriteria(friday, null, null, plan), 100);
        Assert.assertEquals(Utils.ratePlanForCriteria(saturday, null, null, plan), 60);
        Assert.assertEquals(Utils.ratePlanForCriteria(null, "Shimen National", null, plan), 100);
        Assert.assertEquals(Utils.ratePlanForCriteria(null, "Shimen Nanhua", null, plan), 100);
        Assert.assertEquals(Utils.ratePlanForCriteria(saturday, "Other Place", null, plan), 0);
        Assert.assertEquals(Utils.ratePlanForCriteria(friday, "Shimen and Other", null, plan), 60);
    }

    @Test
    public void testRatePastPlans() {
        Plan plan1 = new Plan("Id1", "First", "", 0, "Mon", null, null);
        Plan plan2 = new Plan("Id2", "Second", "", 0, "Mon", null, null);
        Assert.assertEquals(Utils.ratePlanForCriteria(null, null, Arrays.asList(), plan1), 100);
        Assert.assertEquals(Utils.ratePlanForCriteria(null, null, Arrays.asList(plan1), plan1), 0);
        Assert.assertEquals(Utils.ratePlanForCriteria(null, null, Arrays.asList(plan2), plan1), 100);
        Assert.assertEquals(Utils.ratePlanForCriteria(null, null, Arrays.asList(plan1, plan2), plan1), 0);
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
        assertIteratorEquals(Utils.filterAndSortTourResults(null, "Nanhua", null, plans), expectedPlans);

        expectedPlans = Arrays.asList(plan1, plan4).iterator();
        assertIteratorEquals(Utils.filterAndSortTourResults(null, "Shimen National", null, plans), expectedPlans);

        expectedPlans = Arrays.asList(plan1, plan2, plan4, plan3).iterator();
        assertIteratorEquals(Utils.filterAndSortTourResults(thursday, null, null, plans), expectedPlans);

        expectedPlans = Arrays.asList(plan2, plan3, plan1, plan4).iterator();
        assertIteratorEquals(Utils.filterAndSortTourResults(friday, null, null, plans), expectedPlans);

        expectedPlans = Arrays.asList(plan4, plan3).iterator();
        assertIteratorEquals(Utils.filterAndSortTourResults(thursday, "Nanhua", null, plans), expectedPlans);

        expectedPlans = Arrays.asList(plan3).iterator();
        assertIteratorEquals(Utils.filterAndSortTourResults(friday, "Shaoguan", null, plans), expectedPlans);

        expectedPlans = Arrays.asList(plan2).iterator();
        assertIteratorEquals(Utils.filterAndSortTourResults(thursday, "Yangshan", null, plans), expectedPlans);
    }

    @Test
    public void testFilterAndSortTourResultsWithPastPlans() {
        Date thursday = new Date(0);
        Date friday = new Date(86400000);
        Plan plan1 = new Plan("Id1", "Shimen National Forest Tour", "", 0, "Thu", null, null);
        Plan plan2 = new Plan("Id2", "Yangshan Hot Spring Tour", "", 0, "Thu, Fri", null, null);
        Plan plan3 = new Plan("Id3", "Shaoguan sight-seeing tour", "Nanhua Temple", 0, "Fri", null, null);
        Plan plan4 = new Plan("Id3", "National Park Tour", "Also Nanhua Temple", 0, "Thu", null, null);

        List<Plan> pastPlans = Arrays.asList(plan1, plan2);
        List<Plan> plans = Arrays.asList(plan1, plan2, plan3, plan4);

        Iterator<Plan> expectedPlans = Arrays.asList(plan3, plan4).iterator();
        assertIteratorEquals(Utils.filterAndSortTourResults(null, "Nanhua", pastPlans, plans), expectedPlans);

        expectedPlans = Arrays.asList(plan4).iterator();
        assertIteratorEquals(Utils.filterAndSortTourResults(null, "Shimen National", pastPlans, plans), expectedPlans);

        expectedPlans = Arrays.asList(plan4, plan3).iterator();
        assertIteratorEquals(Utils.filterAndSortTourResults(thursday, null, pastPlans, plans), expectedPlans);

        expectedPlans = Arrays.asList(plan3, plan4).iterator();
        assertIteratorEquals(Utils.filterAndSortTourResults(friday, null, pastPlans, plans), expectedPlans);
    }

    private ResultSet createFakeResultSet(List<Object> objects) throws SQLException {
        ResultSet rs = Mockito.mock(ResultSet.class);
        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i) instanceof String) {
                Mockito.when(rs.getString(i + 1)).thenReturn((String) objects.get(i));
            } else if (objects.get(i) instanceof Integer) {
                Mockito.when(rs.getInt(i + 1)).thenReturn((Integer) objects.get(i));
            } else if (objects.get(i) instanceof java.sql.Date) {
                Mockito.when(rs.getDate(i + 1)).thenReturn((java.sql.Date) objects.get(i));
            } else if (objects.get(i) instanceof Timestamp) {
                Mockito.when(rs.getTimestamp(i + 1)).thenReturn((Timestamp) objects.get(i));
            } else if (objects.get(i) instanceof BigDecimal) {
                Mockito.when(rs.getBigDecimal(i + 1)).thenReturn((BigDecimal) objects.get(i));
            }
        }
        return rs;
    }

    @Test
    public void testModels() throws SQLException {
        Booking booking = new Booking("bid", "", new java.sql.Date(0), 1, 1, 1, new BigDecimal(0), new BigDecimal(0), "");
        Customer customer = new Customer("cid", "", "", 20, "12345678", "new");
        Dialogue dialogue = new Dialogue("cid", new java.sql.Timestamp(0), "content");
        Discount discount = new Discount("cid", "pid", new java.sql.Date(0), 1);
        DiscountSchedule discountSchedule = new DiscountSchedule("pid", new java.sql.Date(0), new java.sql.Timestamp(0));
        FAQ faq = new FAQ("question", "answer");
        Plan plan = new Plan("pid", "pname", "description", 1, "departure", new BigDecimal(0), new BigDecimal(0));
        Tour tour = new Tour("pid", new java.sql.Date(0), "guidename", "guideAccount", "hotel", 20, 4);
        BookingStatus bookingStatus = new BookingStatus(tour, plan, Arrays.asList(customer));

        Set set = Sets.newHashSet(booking, customer, dialogue, discount, discountSchedule, faq, plan, tour, bookingStatus);
        Assert.assertTrue(set.contains(booking));
        Assert.assertTrue(set.contains(customer));
        Assert.assertTrue(set.contains(dialogue));
        Assert.assertTrue(set.contains(discount));
        Assert.assertTrue(set.contains(discountSchedule));
        Assert.assertTrue(set.contains(faq));
        Assert.assertTrue(set.contains(plan));
        Assert.assertTrue(set.contains(tour));
        Assert.assertTrue(set.contains(bookingStatus));

        Assert.assertEquals(booking, Booking.fromResultSet(createFakeResultSet(Arrays.asList("bid", "", new java.sql.Date(0), 1, 1, 1, new BigDecimal(0), new BigDecimal(0), ""))));
        Assert.assertEquals(customer, Customer.fromResultSet(createFakeResultSet(Arrays.asList("cid", "", "", 20, "12345678", "new"))));
        Assert.assertEquals(dialogue, Dialogue.fromResultSet(createFakeResultSet(Arrays.asList("cid", new java.sql.Timestamp(0), "content"))));
        Assert.assertEquals(discount, Discount.fromResultSet(createFakeResultSet(Arrays.asList("cid", "pid", new java.sql.Date(0), 1))));
        Assert.assertEquals(discountSchedule, DiscountSchedule.fromResultSet(createFakeResultSet(Arrays.asList("pid", new java.sql.Date(0), new java.sql.Timestamp(0)))));
        Assert.assertEquals(faq, FAQ.fromResultSet(createFakeResultSet(Arrays.asList("question", "answer"))));
        Assert.assertEquals(plan, Plan.fromResultSet(createFakeResultSet(Arrays.asList("pid", "pname", "description", 1, "departure", new BigDecimal(0), new BigDecimal(0)))));
        Assert.assertEquals(tour, Tour.fromResultSet(createFakeResultSet(Arrays.asList("pid", new java.sql.Date(0), "guidename", "guideAccount", "hotel", 20, 4))));
    }
}
