package ru.nordmine.text.generator.handler.region;

import ru.nordmine.StringHelper;
import ru.nordmine.text.generator.AreaSentencePool;
import ru.nordmine.text.generator.SentencePool;

import java.util.List;
import java.util.Map;

public class RegionAreaHandler extends RegionCaptionHandler {

    private static SentencePool sentences = new AreaSentencePool();

    @Override
    public String[] getPossibleCaptions() {
        return new String[]{"площадь", "территория всего", "площадь1", "общая площадь"};
    }

    @Override
    protected void getSentences(String caption, String value, Map<String, List<String>> textParts, String regionName, Map<String, String> rows) {
        addSentenceToMap(textParts, GEO, regionName + " имеет территорию " +
                sentences.next() + " " + StringHelper.roundNumber(StringHelper.getNumberFromString(value))
                + " кв. км");
    }
}
