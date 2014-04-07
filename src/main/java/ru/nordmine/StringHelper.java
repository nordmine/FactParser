package ru.nordmine;

import com.google.common.base.CharMatcher;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;

public class StringHelper {

    private static Logger logger = Logger.getLogger(StringHelper.class);

    public static String upperFirstChar(String textValue) {
        char first = Character.toUpperCase(textValue.charAt(0));
        textValue = first + textValue.substring(1);
        return textValue;
    }

    public static String getNumberFromString(String source) {
        StringBuilder result = new StringBuilder();
        if (!CharMatcher.DIGIT.matchesAnyOf(source)) {
            return " ";
        }
        String sourceBeforeBracket = readBeforeOpenBracket(source);
        source = source.substring(CharMatcher.DIGIT.indexIn(source));
        for (char c : source.toCharArray()) {
            if (CharMatcher.WHITESPACE.matches(c)) {
                continue;
            }
            if (CharMatcher.DIGIT.matches(c)) {
                result.append(c);
            } else {
                break;
            }
        }
        if (sourceBeforeBracket.contains("млн")) {
            result.append("000000");
        }
        if (sourceBeforeBracket.contains("тыс")) {
            result.append("000");
        }
        return result.toString();
    }

    public static String readBeforeOpenBracket(String source) {
        if (source.contains("(")) {
            source = source.substring(0, source.indexOf("(")).trim();
        }
        return source;
    }

    public static String readBeforeAnyOf(String source, char[] chars) {
        for (char c : chars) {
            int charIndex = source.indexOf(c);
            if (charIndex >= 0) {
                source = source.substring(0, charIndex);
            }
        }
        return source;
    }

    public static boolean containsOnlyRussianLetters(String caption) {
        return caption.matches("[а-я ]+$") && caption.length() > 0;
    }

    public static String injectHyperlinks(String source) {
        int startPos = source.indexOf("{");
        int endPos = source.indexOf("}");
        while (startPos >= 0 && endPos > 0) {
            String placeholder = source.substring(startPos, endPos + 1);
            String trimmedPlaceholder = placeholder.substring(1, placeholder.length() - 1);
            String[] parts = trimmedPlaceholder.split("\\|");
            if (parts.length == 2) {
                String address = null;
                try {
                    address = URLEncoder.encode(parts[1], "UTF-8").replace("+", "%20");
                } catch (UnsupportedEncodingException e) {
                    logger.error(e);
                }
                StringBuilder link = new StringBuilder();
                link.append("<a href=\"/").append(parts[0]).append("/");
                link.append(address).append("\">").append(parts[1]).append("</a>");
                source = source.replace(placeholder, link.toString());
            }
            startPos = source.indexOf("{");
            endPos = source.indexOf("}");
        }
        return source;
    }
}
