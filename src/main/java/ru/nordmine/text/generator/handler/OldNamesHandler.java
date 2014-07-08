package ru.nordmine.text.generator.handler;

import ru.nordmine.entities.norm.NormalizedCity;
import ru.nordmine.text.generator.SentencePool;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OldNamesHandler extends CaptionHandler {

    private static SentencePool sentences = new SentencePool(new String[] {
            "В прежние времена встречались такие названия как ",
            "Раньше встречались такие наименования как ",
            "Прежде были известны такие названия как ",
            "В прошлом были и такие наименования как "
    });

    @Override
    public String[] getPossibleCaptions() {
        return new String[] {"прежние названия", "прежнее название"};
    }

    @Override
    protected Map<String, List<String>> getSentences(String caption, String value, NormalizedCity normalizedCity) {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        List<String> lines = new LinkedList<String>();
        lines.add(sentences.next() + value);
        map.put("history", lines);
        return map;
    }

}
