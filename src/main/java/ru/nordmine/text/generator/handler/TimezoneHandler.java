package ru.nordmine.text.generator.handler;

import ru.nordmine.entities.norm.NormalizedCity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TimezoneHandler extends CaptionHandler {

    @Override
    public String[] getPossibleCaptions() {
        return new String[]{"часовой пояс"};
    }

    @Override
    protected Map<String, List<String>> getSentences(String caption, String value, NormalizedCity normalizedCity) {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        if (!value.contains("GMT") && !value.contains("/")) {
            List<String> lines = new LinkedList<String>();
            if (value.contains(", летом")) {
                String utc = value.substring(0, value.indexOf(","));
                String summerUtc = value.substring(value.lastIndexOf("UTC"), value.length());
                lines.add("Зимой местное время по сравнению с московским " + getTextForTimezoneValue(utc) + ", а летом - " + getTextForTimezoneValue(summerUtc));
            } else {
                lines.add("Местное время по сравнению с московским " + getTextForTimezoneValue(value));
            }
            map.put("misc", lines);
        }
        return map;
    }

    private String getTextForTimezoneValue(String timezoneValue) {
        String text = "";
        timezoneValue = timezoneValue.replace("UTC", "").replace("−", "-");
        boolean halfHour = false, threeQuarter = false;
        if (timezoneValue.contains(":00")) {
            timezoneValue = timezoneValue.replace(":00", "");
        }
        if (timezoneValue.contains(":30")) {
            halfHour = true;
            timezoneValue = timezoneValue.replace(":30", ".5");
        }
        if (timezoneValue.contains(":45")) {
            threeQuarter = true;
            timezoneValue = timezoneValue.replace(":45", ".75");
        }
        if (timezoneValue.length() > 0) {
            Float result = 4 - Float.parseFloat(timezoneValue);
            if (result == 0) {
                text = "совпадает";
            }
            if (result > 0) {
                text = "меньше на";
                if (result.intValue() > 0) {
                    text += " " + result.intValue() + " ч";
                }
            }
            if (result < 0) {
                text = "больше на";
                if (Math.abs(result.intValue()) > 0) {
                    text += " " + Math.abs(result.intValue()) + " ч";
                }
            }
            if (halfHour) {
                text += " 30 мин";
            }
            if (threeQuarter) {
                text += " 45 мин";
            }
        }
        return text;
    }
}
