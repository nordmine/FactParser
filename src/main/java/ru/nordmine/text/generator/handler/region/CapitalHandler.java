package ru.nordmine.text.generator.handler.region;

import java.util.List;
import java.util.Map;

public class CapitalHandler extends RegionCaptionHandler {

    @Override
    public String[] getPossibleCaptions() {
        return new String[]{"столица"};
    }

    @Override
    protected void getSentences(String caption, String value, Map<String, List<String>> textParts, String regionName, Map<String, String> rows) {
        if(!rows.containsKey("форма правления")) {
            addSentenceToMap(textParts, GEO, "Столица находится в городе " + value);
        }
    }
}
