package ru.nordmine.text.generator.handler.region;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.springframework.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainCitiesHandler extends RegionCaptionHandler {

    @Override
    public String[] getPossibleCaptions() {
        return new String[]{"крупнейшие города", "крупнейший город", "др. крупные города", "крупные города"};
    }

    @Override
    protected void getSentences(String caption, String value, Map<String, List<String>> textParts, String regionName, Map<String, String> rows) {
        List<String> cityList = Splitter.on(",").trimResults().splitToList(value);
        List<String> allowedNames = new LinkedList<String>();
        for (String city : cityList) {
            if (StringUtils.countOccurrencesOf(city, " ") <= 1) {
                allowedNames.add(city);
            }
        }
        if (!allowedNames.isEmpty()) {
            addSentenceToMap(textParts, GEO,
                    "Наиболее крупными городами являются такие города как "
                            + Joiner.on(", ").skipNulls().join(allowedNames));
        }
    }
}
