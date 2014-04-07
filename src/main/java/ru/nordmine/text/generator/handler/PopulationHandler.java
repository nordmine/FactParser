package ru.nordmine.text.generator.handler;

import org.apache.log4j.Logger;
import ru.nordmine.StringHelper;
import ru.nordmine.entities.norm.NormalizedCity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PopulationHandler extends ComparisonableCaption {

    private static Logger logger = Logger.getLogger(PopulationHandler.class);

    @Override
    public String[] getPossibleCaptions() {
        return new String[]{"население"};
    }

    @Override
    protected Map<String, List<String>> getSentences(String caption, String value, NormalizedCity normalizedCity) {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        String template;
        List<String> lines = new LinkedList<String>();
        if (value.contains("(")) {
            template = "По данным статистики на year год население составляет value чел";
            String year = value.substring(value.indexOf("(") + 1, value.indexOf(")")).trim();
            String number = StringHelper.getNumberFromString(value.substring(0, value.indexOf("("))).trim();
            template = template.replace("value", number).replace("year", year);
        } else {
            template = "По данным статистики, население составляет value чел";
            template = template.replace("value", normalizedCity.getPopulation().toString());
        }
        lines.add(template);
        map.put("geo", lines);
        if (normalizedCity != null) {
            String[] stat = compareOtherCitiesByPopulation(normalizedCity);
            String scaleTemplateCommon = "subject имеет ";
            String[] scaleTemplates = new String[]{
                    scaleTemplateCommon + "примерно столько же жителей",
                    scaleTemplateCommon + "в scale раза меньше жителей",
                    scaleTemplateCommon + "в scale раз меньше жителей"};
            List<String> statLines = new LinkedList<String>();
            statLines.add(populateComparisonString(stat, scaleTemplates));
            statLines.addAll(populateMinMax(caption, "population", normalizedCity.getPopulation(), normalizedCity));
            map.put("stat", statLines);
        }
        return map;
    }

    private String[] compareOtherCitiesByPopulation(NormalizedCity selectedInfobox) {
        String[] statistics = new String[11];
        if (selectedInfobox.getPopulation() != null) {
            String countryName = selectedInfobox.getCountry();
            String name = selectedInfobox.getName();
            List<NormalizedCity> infoboxes = storageService.getNormListByNotNameAndCountry(countryName, name);
            for (NormalizedCity infobox : infoboxes) {
                Integer population = infobox.getPopulation();
                if (population != null) {
                    int result = selectedInfobox.getPopulation() / population;
                    if (result >= 1 && result <= 10) {
                        statistics[result] = infobox.getType() + " {city|" + infobox.getName() + "}";
                    }
                }
            }
        }
        return statistics;
    }
}
