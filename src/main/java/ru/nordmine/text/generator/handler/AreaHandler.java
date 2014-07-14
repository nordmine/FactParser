package ru.nordmine.text.generator.handler;

import ru.nordmine.StringHelper;
import ru.nordmine.entities.norm.NormalizedCity;
import ru.nordmine.text.generator.AreaSentencePool;
import ru.nordmine.text.generator.SentencePool;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AreaHandler extends ComparisonableCaption {

    private static SentencePool sentences = new AreaSentencePool();

    @Override
    public String[] getPossibleCaptions() {
        return new String[]{"площадь"};
    }

    @Override
    protected Map<String, List<String>> getSentences(String caption, String value, NormalizedCity normalizedCity) {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        List<String> lines = new LinkedList<String>();
        String template = "theme имеет площадь " + sentences.next() + " value кв. км";
        lines.add(template.replace("value", StringHelper.roundNumber(StringHelper.getNumberFromString(value))));
        map.put("geo", lines);
        if (normalizedCity != null) {
            String[] stat = compareOtherCitiesByArea(normalizedCity);
            String scaleTemplateCommon = "subject имеет ";
            String[] scaleTemplates = new String[]{
                    scaleTemplateCommon + "примерно такую же площадь",
                    scaleTemplateCommon + "площадь в scale раза меньше",
                    scaleTemplateCommon + "площадь в scale раз меньше"};
            List<String> statLines = new LinkedList<String>();
            statLines.add(populateComparisonString(stat, scaleTemplates));
            statLines.addAll(populateMinMax(caption, "area", normalizedCity.getArea(), normalizedCity));
            map.put("stat", statLines);
        }
        return map;
    }

    private String[] compareOtherCitiesByArea(NormalizedCity selectedInfobox) {
        String[] statistics = new String[11];
        if (selectedInfobox.getArea() != null) {
            String countryName = selectedInfobox.getCountry();
            String name = selectedInfobox.getName();
            List<NormalizedCity> infoboxes = storageService.getNormListByNotNameAndCountry(countryName, name);
            for (NormalizedCity infobox : infoboxes) {
                Integer area = infobox.getArea();
                if (area != null) {
                    int result = selectedInfobox.getArea() / area;
                    if (result >= 1 && result <= 10) {
                        statistics[result] = infobox.getType() + " {city|" + infobox.getName() + "}";
                    }
                }
            }
        }
        return statistics;
    }
}
