package ru.nordmine.services;

import com.google.common.base.Joiner;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.nordmine.StringHelper;
import ru.nordmine.entities.norm.NormalizedCity;
import ru.nordmine.entities.raw.RawCity;
import ru.nordmine.entities.raw.RawRegion;
import ru.nordmine.storage.StorageService;
import ru.nordmine.text.generator.handler.*;
import ru.nordmine.text.generator.handler.region.*;

import java.util.*;

@Service("generatorService")
public class TextGeneratorServiceImpl implements TextGeneratorService {

    private static Logger logger = Logger.getLogger(TextGeneratorServiceImpl.class);

    private CaptionHandler cityHandler;
    private RegionCaptionHandler regionHandler;

    @Autowired
    private NormalizationService normalizationService;

    @Autowired
    private MorpherService morpherService;

    @Autowired
    private StorageService storageService;

    private int themeValueIndex = 0;

    @Override
    public Map<String, String> createArticleForCity(String cityName) {
        initCityHandlers();

        NormalizedCity normalizedCity = storageService.getNormByName(cityName, NormalizedCity.class);
        Map<String, String> resultMap = new HashMap<String, String>();

        // todo мегалополис
        if (!normalizedCity.getType().contains("мегалополис")) {
            themeValueIndex = 0;
            cityHandler.resetState();
            Map<String, List<String>> textParts = new HashMap<String, List<String>>();

            RawCity rawCity = storageService.getRawByName(normalizedCity.getName(), RawCity.class);
            logger.info(rawCity.getText());
            logger.info(rawCity.getRaw());
            Document d = null;
            try {
                d = DocumentHelper.parseText(rawCity.getRaw());
            } catch (DocumentException e) {
                e.printStackTrace();
            }

            if (d != null) {
                String type = normalizedCity.getType();
                String subject = StringHelper.readBeforeOpenBracket(normalizedCity.getName());
                String[] themeValues = new String[]{subject, type};
                logger.info(StringHelper.upperFirstChar(type) + " " + subject);
                List<Node> rows = d.selectNodes("//info/row");

                for (Node row : rows) {
                    String caption = row.selectSingleNode("caption").getText().toLowerCase();
                    String value = StringHelper.readBeforeAnyOf(row.selectSingleNode("value").getText(), new char[]{'(', '[', '|'}).trim();
                    logger.info(caption + ": " + value);
                    addEntriesToFirstMap(textParts, cityHandler.process(caption, value, normalizedCity));
                }
                for (Map.Entry<String, List<String>> entry : textParts.entrySet()) {
                    List<String> curList = entry.getValue();
                    for (int i = 0; i < curList.size(); i++) {
                        String line = curList.get(i);
                        if (line != null) {
                            String modifiedLine = StringHelper.upperFirstChar(populateTheme(themeValues, curList.get(i)));
                            curList.set(i, StringHelper.injectHyperlinks(modifiedLine));
                        }
                    }
                }
            }
            String[] textPartsOrder = new String[]{"region", "geo", "history", "misc"};
            StringBuilder mainTextBuilder = new StringBuilder();
            for (String textPart : textPartsOrder) {
                if (textParts.containsKey(textPart)) {
                    mainTextBuilder.append(Joiner.on(". ").skipNulls().join(textParts.get(textPart))).append(". ");
                }
            }
            resultMap.put("text", "<p>" + mainTextBuilder.toString() + "</p>");

            if (textParts.containsKey("stat")) {
                resultMap.put("stat", "<ul><li>" + Joiner.on(".</li><li>").skipNulls().join(textParts.get("stat")) + "</li></ul>");
            }

            logger.info(resultMap.get("text"));
            logger.info("Немного статистики:");
            logger.info(resultMap.get("stat"));
            logger.info("**************************");
        }
        return resultMap;
    }

    @Override
    public Map<String, String> createArticleForRegion(String regionName) {
        Map<String, String> map = new HashMap<String, String>();
        RawRegion rawRegion = storageService.getRawByNameWhereRawNotEmpty(regionName, RawRegion.class);

        if (rawRegion != null) {
            Map<String, Integer> stat = new HashMap<String, Integer>();
            Map<String, String> normalizedRows = normalizationService.normalizeRegion(stat, rawRegion);
            logger.info(normalizedRows);

            initRegionHandlers();
            Map<String, List<String>> textParts = new TreeMap<String, List<String>>();

            for (String key : normalizedRows.keySet()) {
                regionHandler.process(key, textParts, regionName, normalizedRows);
            }

            StringBuilder mainTextBuilder = new StringBuilder();
            for (Map.Entry<String, List<String>> textPart : textParts.entrySet()) {
                if(textPart.getKey().equals(RegionCaptionHandler.MAIN_FOR_LIST)) {
                    continue;
                }
                mainTextBuilder.append(Joiner.on(". ").skipNulls().join(textPart.getValue())).append(". ");
            }

            String text = mainTextBuilder.toString();
            logger.info(text);
            map.put("text", text);
            if (textParts.containsKey(RegionCaptionHandler.MAIN_FOR_LIST)) {
                map.put("polity", Joiner.on(". ").skipNulls().join(textParts.get(RegionCaptionHandler.MAIN_FOR_LIST)) + ".");
            }
        }
        return map;
    }

    private void addEntriesToFirstMap(Map<String, List<String>> mainMap, Map<String, List<String>> currentMap) {
        for (Map.Entry<String, List<String>> entry : currentMap.entrySet()) {
            if (mainMap.containsKey(entry.getKey())) {
                mainMap.get(entry.getKey()).addAll(entry.getValue());
            } else {
                mainMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private void initCityHandlers() {
        if (cityHandler == null) {
            CaptionHandler handler1, handler2, handler3, handler4, handler5,
                    handler6, handler7, handler8, handler9, handler10, handler11, handler12, handler13;
            cityHandler = new AreaHandler();
            cityHandler.setStorageService(storageService);
            cityHandler.setMorpherService(morpherService);
            handler1 = cityHandler.add(new AreaHandler());
            handler2 = handler1.add(new CityYearHandler());
            handler3 = handler2.add(new DensityHandler());
            handler4 = handler3.add(new FirstMentionHandler());
            handler5 = handler4.add(new FoundationYearHandler());
            handler6 = handler5.add(new HeightHandler());
            handler7 = handler6.add(new LanguageHandler());
            handler8 = handler7.add(new MayorHandler());
            handler9 = handler8.add(new PopulationHandler());
            handler10 = handler9.add(new RegionHandler());
            handler11 = handler10.add(new TimezoneHandler());
            handler12 = handler11.add(new ClimateHandler());
            handler13 = handler12.add(new CoordinateHandler());
            handler13.add(new OldNamesHandler());
        }
    }

    private void initRegionHandlers() {
        if (regionHandler == null) {
            RegionCaptionHandler handler1, handler2, handler3, handler4, handler5, handler6, handler7, handler8, handler9;
            regionHandler = new PolityHandler();
            regionHandler.setMorpherService(morpherService);

            Set<String> allCityNames = new HashSet<String>();
            allCityNames.addAll(storageService.getAllCityNames());
            regionHandler.setAllCityNames(allCityNames);

            handler1 = regionHandler.add(new MainCitiesHandler());
            handler2 = handler1.add(new RegionAreaHandler());
            handler3 = handler2.add(new RegionCenterHandler());
            handler4 = handler3.add(new RegionLanguageHandler());
            handler5 = handler4.add(new RegionPopulationHandler());
            handler6 = handler5.add(new CurrencyHandler());
            handler7 = handler6.add(new GovernorHandler());
            handler8 = handler7.add(new RegionDensityHandler());
            handler9 = handler8.add(new RegionStatusHandler());
            handler9.add(new CapitalHandler());
        }
    }

    private String populateTheme(String[] themeValues, String template) {
        if (template.contains("theme")) {
            template = template.replace("theme", themeValues[themeValueIndex]);
            themeValueIndex++;
            if (themeValueIndex == themeValues.length) {
                themeValueIndex = 0;
            }
        }
        return template;
    }
}
