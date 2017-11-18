package com.example.bot.spring;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Utils {
    /**
     * Syntactic sugar for creating an array of objects
     * @param xs Any number of objects
     * @return An array containing each parameter supplied
     */
    public static Object[] params(Object... xs) {
        return xs;
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
