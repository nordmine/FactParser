package ru.nordmine.text.generator.handler;

import ru.nordmine.StringHelper;
import ru.nordmine.entities.norm.NormalizedCity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LanguageHandler extends CaptionHandler {

    @Override
    public String[] getPossibleCaptions() {
        return new String[] {"официальный язык"};
    }

    @Override
    protected Map<String, List<String>> getSentences(String caption, String value, NormalizedCity normalizedCity) {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        List<String> lines = new LinkedList<String>();
        String template = "В качестве официального языка местные жители используют " + StringHelper.readBeforeOpenBracket(value).toLowerCase();
        lines.add(template);
        map.put("misc", lines);
        return map;
    }
}
