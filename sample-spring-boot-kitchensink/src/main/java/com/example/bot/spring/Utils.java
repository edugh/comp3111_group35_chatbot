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

    public static int getDateOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    public static int getDateOfWeek(String abbreviation) {
        List<String> abbreviations = Arrays.asList("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat");
        return abbreviations.indexOf(abbreviation) + 1;
    }

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

    public static Iterator<Plan> filterAndSortTourResults(Date date, String keywords, List<Plan> pastPlans, List<Plan> plans) {
        return plans.stream()
                .map(plan -> Pair.of(plan, ratePlanForCriteria(date, keywords, pastPlans, plan)))
                .filter(o -> o.getRight() > 50)
                .sorted((o1, o2) -> o2.getRight() - o1.getRight())
                .map(Pair::getLeft)
                .iterator();
    }

    // TODO: Should we use some service like wit.ai for our milestone 3?
    // or at least a better matching thing...
    public static boolean stupidFuzzyMatch(String question, String text) {
        Set<String> targets = new HashSet<>(Arrays.asList(question.split(" ")));
        String[] text_words = text.split(" ");
        double num_words = (targets.size() + text_words.length) / 2.;
        double matches = Arrays.stream(text_words).mapToInt(text_word -> targets.contains(text_word)? 1:0).sum();
        return (matches / num_words) > 0.5;
    }

    public static boolean isYes(String answer){
        if(answer.toLowerCase().contains(new String("yes").toLowerCase())){
            return true;
        }
        //TODO: yes, yep, yeah, ok, of course, sure, why not
        return false;
    }

    public static java.sql.Date getDateFromText(String answer)throws ParseException {
        //TODO: standardize YYYYMMDD
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        java.util.Date parsed = format.parse(answer);
        return(new java.sql.Date(parsed.getTime()));
    }

    public static int getIntFromText(String answer){
        //TODO: filterString
        return Integer.parseInt(answer);
    }

    public static String filterString(String answer){
        //TODO: I'm XX -> XX
        return answer;
    }

    public static String getGender(String answer){
        String charGender = answer.substring(0,1).toUpperCase();
        if(charGender.matches("[A-Z]")){
            return charGender;
        }
        else
            return "N";
    }
}
