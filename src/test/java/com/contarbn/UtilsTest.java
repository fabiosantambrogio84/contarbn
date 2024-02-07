package com.contarbn;

import com.contarbn.model.reports.SchedaTecnicaAnalisiDataSource;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UtilsTest {

    @Test
    public void firstTest(){
        String input = "< 10^5 UFC/gr AND < 10^2 UFC/gr";
        //String input = "abcdef";

        System.out.println(SchedaTecnicaAnalisiDataSource.parseRisultato(input));
    }

    @Test
    public void secondTest(){
        //String composizione = "<p><strong>uova</strong>,olio e.v.,<strong>acciuga, sale</strong>, <strong>ciao</strong>, <strong>ciao2</strong></p>";
        String composizione = "<p></p>";

        // Define the pattern to match words between <strong> and </strong>
        Pattern pattern = Pattern.compile("<strong>(.*?)</strong>");

        // Create a matcher with the input string
        Matcher matcher = pattern.matcher(composizione);

        // Find and print all matches
        while (matcher.find()) {
            String matchedGroup = matcher.group(1); // Extract the content between <strong> and </strong>
            System.out.println("-> "+matchedGroup);
            // Split the content into individual words
            String[] words = matchedGroup.split("\\s*,\\s*");

            // Print each word
            for (String word : words) {
                System.out.println(word);
            }
        }
    }
}