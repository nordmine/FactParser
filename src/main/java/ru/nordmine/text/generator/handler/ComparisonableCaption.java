package ru.nordmine.text.generator.handler;

import ru.nordmine.entities.norm.NormalizedCity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

public abstract class ComparisonableCaption extends CaptionHandler {

    protected String populateComparisonString(String[] stat, String[] scaleTemplates) {
        String result = null;
        if (stat[1] != null) {
            result = getResultComparisonString(scaleTemplates[0], stat[1], 1);
        } else {
            int i = 2;
            for (; i < 5; i++) {
                if (stat[i] != null) {
                    result = getResultComparisonString(scaleTemplates[1], stat[i], i);
                    break;
                }
            }
            if (i == 5) {
                for (; i <= 10; i++) {
                    if (stat[i] != null) {
                        result = getResultComparisonString(scaleTemplates[2], stat[i], i);
                        break;
                    }
                }
            }
        }
        return result;
    }

    private String getResultComparisonString(String scaleTemplate, String value, int scale) {
        String address = null;
        try {
            address = URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return scaleTemplate.replace("subject", value)
                .replace("scale", Integer.toString(scale));
    }

    // todo порефакторить
    protected List<String> populateMinMax(String caption, String fieldName, Integer fieldValue, NormalizedCity infobox) {
        List<String> lines = new LinkedList<String>();
        String countryName = infobox.getCountry();
        String name = infobox.getName();
        if (fieldValue != null) {
            String captionForm = "<b>" + morpherService.loadFormsFromStorage(caption).getSingleRod() + "</b>";
            {
                String line = populateAgregation("min", "страна", fieldName, fieldValue, countryName, name, captionForm, null);
                lines.add(line);

                line = populateAgregation("max", "страна", fieldName, fieldValue, countryName, name, captionForm, null);
                lines.add(line);
            }

            if (infobox.getSecondLocation() != null) {
                {
                    String regionName = infobox.getSecondLocation();
                    String line = populateAgregation("min", "регион", fieldName, fieldValue, countryName, name, captionForm, regionName);
                    lines.add(line);

                    line = populateAgregation("max", "регион", fieldName, fieldValue, countryName, name, captionForm, regionName);
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    private String populateAgregation(String function, String regionType, String fieldName, Integer fieldValue, String countryName, String name, String captionForm, String regionName) {
        String line = null;
        Integer agregatedValue = null;
        if (regionName == null) {
            agregatedValue = storageService.agregateNormByNameAndCountry(function, fieldName, countryName, name);
        } else {
            agregatedValue = storageService.agregateNormByNameAndRegion(function, fieldName, countryName, name, regionName);
        }
        if (agregatedValue != null) {
            String functionString = null;
            if (function.equals("min")) {
                if (agregatedValue.intValue() > fieldValue.intValue()) {
                    functionString = "маленький";
                }
            }
            if (function.equals("max")) {
                if (agregatedValue.intValue() < fieldValue.intValue()) {
                    functionString = "большой";
                }
            }
            if (functionString != null) {
                line = "Самый " + functionString + " показатель " + captionForm + " в "
                        + morpherService.loadFormsFromStorage(regionType).getSinglePred();

                if (regionName == null) {
                    line += " {region|" + countryName + "}";
                } else {
                    line += " {region|" + regionName + "}";
                }
            }
        }
        return line;
    }
}
