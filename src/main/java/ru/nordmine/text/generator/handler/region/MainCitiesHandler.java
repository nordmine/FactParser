package ru.nordmine.text.generator.handler.region;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.springframework.util.StringUtils;
import ru.nordmine.text.generator.SentencePool;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainCitiesHandler extends RegionCaptionHandler {

    private static SentencePool sentences = new SentencePool(
            new String[]{
                    "Наиболее крупными городами являются такие города как ",
                    "К наиболее крупным городам относят ",
                    "Эти города считаются самыми крупными: ",
                    "Крупнейшими городами считаются ",
                    "Основными городами можно считать ",
                    "Туристам следует постетить такие города как ",
                    "Более полное впечатление о стране вам помогут составить такие города как "
            }
    );

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
                    sentences.next()
                            + Joiner.on(", ").skipNulls().join(allowedNames)
            );
        }
    }
}
