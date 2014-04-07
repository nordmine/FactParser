package ru.nordmine.text.generator.handler;

import ru.nordmine.StringHelper;
import ru.nordmine.entities.norm.NormalizedCity;
import ru.nordmine.text.generator.SentencePool;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MayorHandler extends CaptionHandler {

    private static SentencePool sentences = new SentencePool(new String[]{
            "Должность value занимает ",
            "На посту value состоит ",
            "В качестве value работает ",
            "Кресло value занимает "
    });

    @Override
    public String[] getPossibleCaptions() {
        return new String[]{
                "бургомистр", "мэр коммуны", "мэр", "староста", "кмет", "глава"
        };
    }

    @Override
    protected Map<String, List<String>> getSentences(String caption, String value, NormalizedCity normalizedCity) {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        List<String> lines = new LinkedList<String>();
        String captionForm = morpherService.loadFormsFromStorage(caption).getSingleRod();
        String sentence = sentences.nextWithValue(captionForm);
        lines.add(sentence + StringHelper.readBeforeOpenBracket(value));
        map.put("misc", lines);
        return map;
    }
}
