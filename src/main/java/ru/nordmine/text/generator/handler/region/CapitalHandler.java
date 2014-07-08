package ru.nordmine.text.generator.handler.region;

import ru.nordmine.text.generator.SentencePool;

import java.util.List;
import java.util.Map;

public class CapitalHandler extends RegionCaptionHandler {

    private static SentencePool sentences = new SentencePool(new String[] {
          "value является столицей",
          "Столицей является город value",
          "Столица находится в городе value",
          "value имеет статус столицы",
          "Город value был выбран в качестве столицы"
    });

    @Override
    public String[] getPossibleCaptions() {
        return new String[]{"столица"};
    }

    @Override
    protected void getSentences(String caption, String value, Map<String, List<String>> textParts, String regionName, Map<String, String> rows) {
        if(!rows.containsKey("форма правления")) {
            addSentenceToMap(textParts, GEO, sentences.nextWithValue(value));
        }
    }
}
