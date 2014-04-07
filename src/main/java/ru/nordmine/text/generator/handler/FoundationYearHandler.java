package ru.nordmine.text.generator.handler;

import com.google.common.base.Joiner;
import ru.nordmine.entities.norm.NormalizedCity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FoundationYearHandler extends ComparisonableCaption {

    @Override
    public String[] getPossibleCaptions() {
        return new String[]{"основан"};
    }

    @Override
    protected Map<String, List<String>> getSentences(String caption, String value, NormalizedCity normalizedCity) {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        List<String> lines = new LinkedList<String>();
        if (normalizedCity.getFoundationYear() != null) {
            String template = "theme впервые упоминается в value году".replace("value", normalizedCity.getFoundationYear().toString());
            lines.add(template);
            lines.add(getOtherCitiesByFoundationYear(normalizedCity));
        }
        map.put("history", lines);
        return map;
    }

    private String getOtherCitiesByFoundationYear(NormalizedCity curInfobox) {
        String line = null;
        if (curInfobox.getFoundationYear() != null) {
            String type = curInfobox.getType();
            String name = curInfobox.getName();
            Integer foundationYear = curInfobox.getFoundationYear();
            List<NormalizedCity> infoboxList = storageService.getNormListByNotNameAndTypeAndFoundationYear(type, name, foundationYear);

            if (!infoboxList.isEmpty()) {
                List<String> cities = new LinkedList<String>();
                for (NormalizedCity infobox : infoboxList) {
                    if (cities.size() < 3) {
                        cities.add("{city|" + infobox.getName() + "}");
                    }
                }
                String typesName = morpherService.loadFormsFromStorage(curInfobox.getType()).getMultipleImen();
                line = "В это же время возникли такие " + typesName + " как " + Joiner.on(", ").join(cities);
            }
        }
        return line;
    }
}
