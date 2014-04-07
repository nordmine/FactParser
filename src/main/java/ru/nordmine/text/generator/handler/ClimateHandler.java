package ru.nordmine.text.generator.handler;

import ru.nordmine.entities.norm.NormalizedCity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ClimateHandler extends CaptionHandler {

    @Override
    public String[] getPossibleCaptions() {
        return new String[]{"тип климата"};
    }

    @Override
    protected Map<String, List<String>> getSentences(String caption, String value, NormalizedCity normalizedCity) {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        List<String> lines = new LinkedList<String>();
        lines.add("theme имеет " + value + " тип климата");
        map.put("geo", lines);
        return map;
    }
}
