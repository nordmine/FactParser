package ru.nordmine.text.generator.handler.region;

import com.google.common.base.Splitter;
import ru.nordmine.StringHelper;
import ru.nordmine.services.MorpherService;

import java.util.*;

public abstract class RegionCaptionHandler {

    protected RegionCaptionHandler nextHandler;
    protected MorpherService morpherService;
    protected Set<String> allCityNames;

    public Set<String> getAllCityNames() {
        return allCityNames;
    }

    public void setAllCityNames(Set<String> allCityNames) {
        this.allCityNames = allCityNames;
    }

    public static final String MAIN_FOR_LIST = "0.main-for-list";
    public static final String MAIN = "1.main";
    public static final String GEO = "2.geo";
    public static final String STAT = "3.stat";
    public static final String GOVERNOR = "4.governor";
    public static final String MISC = "5.misc";

    protected RegionCaptionHandler() {
    }

    public void resetState() {
        if (nextHandler != null) {
            nextHandler.resetState();
        }
    }

    public RegionCaptionHandler add(RegionCaptionHandler handler) {
        nextHandler = handler;
        handler.morpherService = this.morpherService;
        handler.allCityNames = this.allCityNames;
        return handler;
    }

    public void process(String caption, Map<String, List<String>> textParts, String regionName, Map<String, String> rows) {
        List<String> possibleCaptions = Arrays.asList(getPossibleCaptions());
        String value = rows.get(caption);
        if (value != null) {
            if (possibleCaptions.contains(caption)) {
                getSentences(caption, value, textParts, regionName, rows);
            } else if (nextHandler != null) {
                nextHandler.process(caption, textParts, regionName, rows);
            }
        }

    }

    public abstract String[] getPossibleCaptions();

    protected abstract void getSentences(String caption, String value, Map<String, List<String>> textParts, String regionName, Map<String, String> rows);

    protected String getEthno(Map<String, String> rows) {
        String citizens = "Местные жители";
        if (rows.containsKey("этнохороним")) {
            String ethno = rows.get("этнохороним");
            List<String> ethnoForms = Splitter.on(",").trimResults().splitToList(ethno);
            for (String ethnoForm : ethnoForms) {
                ethnoForm = ethnoForm.toLowerCase();
                if (ethnoForm.endsWith("ы") || ethnoForm.endsWith("е") || ethnoForm.endsWith("и")) {
                    citizens = StringHelper.upperFirstChar(ethnoForm);
                    break;
                }
            }
        }
        return citizens;
    }

    protected void addSentenceToMap(Map<String, List<String>> map, String key, String sentence) {
        if (!map.containsKey(key)) {
            map.put(key, new LinkedList<String>());
        }
        map.get(key).add(sentence);
    }

    public MorpherService getMorpherService() {
        return morpherService;
    }

    public void setMorpherService(MorpherService morpherService) {
        this.morpherService = morpherService;
    }
}
