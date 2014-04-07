package ru.nordmine.text.generator.handler;

import ru.nordmine.StringHelper;
import ru.nordmine.entities.norm.NormalizedCity;
import ru.nordmine.text.generator.AreaSentencePool;
import ru.nordmine.text.generator.SentencePool;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DensityHandler extends ComparisonableCaption {

    private static SentencePool sentences = new AreaSentencePool();

    @Override
    public String[] getPossibleCaptions() {
        return new String[]{"плотность"};
    }

    @Override
    protected Map<String, List<String>> getSentences(String caption, String value, NormalizedCity normalizedCity) {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        List<String> lines = new LinkedList<String>();
        String template = "theme имеет плотность населения " + sentences.next() + " value чел/кв. км";
        template = template.replace("value", StringHelper.getNumberFromString(value));
        lines.add(template);
        map.put("geo", lines);

        if (normalizedCity != null) {
            String[] stat = compareOtherCitiesByDensity(normalizedCity);
            String scaleTemplateCommon = "subject имеет ";
            String[] scaleTemplates = new String[]{
                    scaleTemplateCommon + "примерно такую же плотность населения",
                    scaleTemplateCommon + "плотность населения в scale раза меньше",
                    scaleTemplateCommon + "плотность населения в scale раз меньше"};
            List<String> statLines = new LinkedList<String>();
            statLines.add(populateComparisonString(stat, scaleTemplates));
            statLines.addAll(populateMinMax(caption, "density", normalizedCity.getDensity(), normalizedCity));
            map.put("stat", statLines);
        }

        return map;
    }

    private String[] compareOtherCitiesByDensity(NormalizedCity selectedInfobox) {
        String[] statistics = new String[11];
        if (selectedInfobox.getDensity() != null) {
            List<NormalizedCity> infoboxes = storageService.getNormListByNotNameAndCountry(selectedInfobox.getCountry(), selectedInfobox.getName());
            for (NormalizedCity infobox : infoboxes) {
                Integer density = infobox.getDensity();
                if (density != null) {
                    int result = selectedInfobox.getDensity() / density;
                    if (result >= 1 && result <= 10) {
                        statistics[result] = infobox.getType() + " {city|" + infobox.getName() + "}";
                    }
                }
            }
        }
        return statistics;
    }
}
