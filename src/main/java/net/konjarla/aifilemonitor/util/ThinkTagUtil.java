package net.konjarla.aifilemonitor.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThinkTagUtil {
    public static String removeThinkTags(String text) {
        if (text == null) {
            return null;
        }
        // Regex to match <think> tags and everything in between, including newlines

        if (text.contains("\\u003C".toLowerCase())) {
            text = text.replaceAll("(?i)\\\\u003C", "<");
            text = text.replaceAll("(?i)\\\\u003E", ">");
            text = text.replaceAll("(?i)\\\\u002F", "/");
        }

        // String regex = "<think>(?:.|\n)*?</think>";
        // Pattern pattern = Pattern.compile(regex);
        Pattern pattern = Pattern.compile("<think>.*?</think>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);

        return matcher.replaceAll("");
    }

    public static String getThinkTags(String text) {
        if (text == null) {
            return null;
        }

        if (text.contains("\\u003C".toLowerCase())) {
            text = text.replaceAll("(?i)\\\\u003C", "<");
            text = text.replaceAll("(?i)\\\\u003E", ">");
            text = text.replaceAll("(?i)\\\\u002F", "/");
        }
        // Regex to match <think> tags and everything in between, including newlines
        // Pattern pattern = Pattern.compile("(?s)<think>(.*?)</think>");
        Pattern pattern = Pattern.compile("<think>.*?</think>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }
}
