package ru.nordmine.text.generator.handler;

import ru.nordmine.StringHelper;
import ru.nordmine.entities.norm.NormalizedCity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HeightHandler extends CaptionHandler {

    @Override
    public String[] getPossibleCaptions() {
        return new String[]{"высота", "высота центра", "высота нум"};
    }

    @Override
    protected Map<String, List<String>> getSentences(String caption, String value, NormalizedCity normalizedCity) {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        List<String> lines = new LinkedList<String>();
        String template = "Высота над уровем моря составляет value м";
        lines.add(template.replace("value", StringHelper.getNumberFromString(value)));
        map.put("geo", lines);
        return map;
    }
}
