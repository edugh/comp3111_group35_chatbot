package com.example.bot.spring;

import com.example.bot.spring.model.Plan;
import org.apache.commons.lang3.tuple.Pair;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Utils {
    /**
     * Syntactic sugar for creating an array of objects
     * @param xs Any number of objects
     * @return An array containing each parameter supplied
     */
    public static Object[] params(Object... xs) {
        return xs;
    }

    /**
     * Returns the Calendar day for the given Date object
     * @param date the date to return as a weekday
     * @return the weekday of the supplied date
     */
    public static int getDateOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * Returns the Calendar day for the given weekday abbreviation
     * @param abbreviation word to return day for
     * @return the weekday of supplied date
     */
    public static int getDateOfWeek(String abbreviation) {
        List<String> abbreviations = Arrays.asList("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat");
        return abbreviations.indexOf(abbreviation) + 1;
    }

    /**
     * Gives a rating from 0-100 for whether a plan is relevant to a user, given their supplied
     * date, search keywords, and past bookings.
     * @param date the date to search for tours on
     * @param search the keywords to search tours for
     * @param pastPlans past plans the user has booked so we do not recommend them again
     * @param plan the plan to give a rating for
     * @return rating as to whether we should surface the plan
     */
    public static int ratePlanForCriteria(Date date, String search, List<Plan> pastPlans, Plan plan) {
        int score = 100;
        if (date != null) {
            boolean matchedDate = Arrays.asList(plan.departure.split(", ")).stream().anyMatch(abbr -> getDateOfWeek(date) == getDateOfWeek(abbr));
            if (!matchedDate) {
                score -= 40;
            }
        }

        if (search != null) {
            String fullSearchableText = plan.name + plan.shortDescription;
            List<String> keywords = Arrays.asList(search.split(" "));
            score -= 60 * keywords.stream().mapToDouble(keyword -> fullSearchableText.contains(keyword) ? 0 : 1).sum() / keywords.size();
        }

        if (pastPlans != null && !pastPlans.isEmpty()) {
            if (pastPlans.contains(plan)) {
                score = 0;
            }
        }

        return score;
    }

    /**
     * Given a list of plans and some search parameters, see ratePlanForCriteria for param details,
     * sort and filter the plans by their surface rating
     * @param date see ratePlanForCriteria for details
     * @param keywords see ratePlanForCriteria for details
     * @param pastPlans see ratePlanForCriteria for details
     * @param plans list of plans to sort/filter
     * @return the sorted and filtered version of the list
     */
    public static Iterator<Plan> filterAndSortTourResults(Date date, String keywords, List<Plan> pastPlans, List<Plan> plans) {
        return plans.stream()
                .map(plan -> Pair.of(plan, ratePlanForCriteria(date, keywords, pastPlans, plan)))
                .filter(o -> o.getRight() > 50)
                .sorted((o1, o2) -> o2.getRight() - o1.getRight())
                .map(Pair::getLeft)
                .iterator();
    }

    /**
     * Returns the fraction of words common between two two passed strings
     * @param question
     * @param text
     * @return returns the number of matched words by half the total number of passed words
     */
    public static boolean stupidFuzzyMatch(String question, String text) {
        Set<String> targets = new HashSet<>(Arrays.asList(question.split(" ")));
        String[] text_words = text.split(" ");
        double num_words = (targets.size() + text_words.length) / 2.;
        double matches = Arrays.stream(text_words).mapToInt(text_word -> targets.contains(text_word)? 1:0).sum();
        return (matches / num_words) > 0.5;
    }

    /**
     * Parses a date string in the format 'yyyy/MM/dd' into an sql date
     * @param dateString String to parse into date
     * @return The parsed java.sql.Date
     * @throws ParseException
     */
    public static java.sql.Date getDateFromText(String dateString) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        java.util.Date parsed = format.parse(dateString);
        return(new java.sql.Date(parsed.getTime()));
    }

    /**
     * Given a String version of gender return the upper cased first character. On error return 'N'
     * @param gender Gender to parse into character
     * @return Upper cased first character of gender
     */
    public static String getGender(String gender) {
        String charGender = gender.substring(0,1).toUpperCase();
        if(charGender.matches("[A-Z]")){
            return charGender;
        } else {
            return "N";
        }
    }
}
