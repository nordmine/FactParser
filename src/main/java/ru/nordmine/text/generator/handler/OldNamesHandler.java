package ru.nordmine.text.generator.handler;

import ru.nordmine.entities.norm.NormalizedCity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OldNamesHandler extends CaptionHandler {

    @Override
    public String[] getPossibleCaptions() {
        return new String[] {"прежние названия", "прежнее название"};
    }

    @Override
    protected Map<String, List<String>> getSentences(String caption, String value, NormalizedCity normalizedCity) {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        List<String> lines = new LinkedList<String>();
        lines.add("В прежние времена встречались такие названия как " + value);
        map.put("history", lines);
        return map;
    }

}
