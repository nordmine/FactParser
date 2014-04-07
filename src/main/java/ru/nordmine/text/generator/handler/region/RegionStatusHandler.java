package ru.nordmine.text.generator.handler.region;

import java.util.List;
import java.util.Map;

public class RegionStatusHandler extends RegionCaptionHandler {

    @Override
    public String[] getPossibleCaptions() {
        return new String[]{"статус"};
    }

    @Override
    protected void getSentences(String caption, String value, Map<String, List<String>> textParts, String regionName, Map<String, String> rows) {
        if (value != null && rows.containsKey("страна")) {
            addSentenceToMap(textParts, GEO, regionName + " - " + value.toLowerCase() + " страны " + rows.get("страна"));
        }
    }
}
