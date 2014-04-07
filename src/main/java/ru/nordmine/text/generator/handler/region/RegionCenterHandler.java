package ru.nordmine.text.generator.handler.region;

import java.util.List;
import java.util.Map;

public class RegionCenterHandler extends RegionCaptionHandler {

    @Override
    public String[] getPossibleCaptions() {
        return new String[]{"административный центр", "административный центр", "адм. центр"};
    }

    @Override
    protected void getSentences(String caption, String value, Map<String, List<String>> textParts, String regionName, Map<String, String> rows) {
        addSentenceToMap(textParts, GEO, "Административный центр - " + value);
    }
}
