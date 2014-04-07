package ru.nordmine.text.generator.handler;

import com.google.common.base.CharMatcher;
import ru.nordmine.StringHelper;
import ru.nordmine.entities.norm.NormalizedCity;

import java.util.*;

public class FirstMentionHandler extends CaptionHandler {

    @Override
    public String[] getPossibleCaptions() {
        return new String[] {"первое упоминание"};
    }

    @Override
    protected Map<String, List<String>> getSentences(String caption, String value, NormalizedCity normalizedCity) {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        List<String> lines = new LinkedList<String>();
        String template;
        if(value.contains("век")) {
            template = "theme впервые упоминается в value веке".replace("value", CharMatcher.anyOf("IVX").retainFrom(value));
        } else {
            template = "theme впервые упоминается в value году".replace("value", StringHelper.getNumberFromString(value));
        }
        lines.add(template);
        map.put("history", lines);
        return map;
    }
}
