package ru.nordmine.services;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.nordmine.StringHelper;
import ru.nordmine.entities.norm.Norm;
import ru.nordmine.entities.norm.NormalizedCity;
import ru.nordmine.entities.raw.RawCity;
import ru.nordmine.entities.raw.RawRegion;
import ru.nordmine.storage.StorageService;
import ru.nordmine.text.generator.handler.RegionHandler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@Service("normalizationService")
public class NormalizationServiceImpl implements NormalizationService {

    private static Logger logger = Logger.getLogger(NormalizationServiceImpl.class);

    @Autowired
    private StorageService storageService;

    @Override
    public int normalizeRegions(int skipCount, int takeCount) {
        int affectedRows = 0;
        Map<String, Integer> stat = new TreeMap<String, Integer>();
        List<RawRegion> raws = storageService.getRawListWhereRawNotEmpty(RawRegion.class, skipCount, takeCount);

        for (RawRegion raw : raws) {
            normalizeRegion(stat, raw);
            affectedRows++;
        }

        for (Map.Entry<String, Integer> entry : stat.entrySet()) {
            entry.setValue((int) (((double) entry.getValue()) / ((double) raws.size()) * 100));
            if (entry.getValue() >= 0) {
                logger.info(entry.getKey() + "; count=" + entry.getValue() + "%");
            }
        }
        return affectedRows;
    }

    public Map<String, String> normalizeRegion(Map<String, Integer> stat, RawRegion raw) {
        Map<String, String> normalizedRows = new HashMap<String, String>();
        logger.info("raw text: " + raw.getText());
            /*
            List<NormRegion> normInfoboxes = em.createQuery("from NormRegion n where n.name = :name", NormRegion.class)
                    .setParameter("name", raw.getText())
                    .getResultList();

            Norm normInfobox = new NormRegion();
            if (normInfoboxes.size() > 0) {
                normInfobox = normInfoboxes.get(0);
            }*/

        String rawText = raw.getRaw();
        Document d = parseDocumentFromText(rawText);

//            normInfobox.setName(raw.getText());
        List<Node> rows = d.selectNodes("//info/row");

        for (Node rowNode : rows) {
            String caption = rowNode.selectSingleNode("caption").getText().toLowerCase();
            String value = rowNode.selectSingleNode("value").getText();

            List<String> captionList = Splitter.on('\n').trimResults().splitToList(caption);
            List<String> valueList = new LinkedList<String>();
            if (captionList.size() > 1) {
                logger.info(value);
                valueList = Splitter.on('\n').splitToList(value);
                logger.info(valueList);
            } else {
                valueList.add(value);
            }

            // todo правильная обработка многострочных ячеек
            int difference = captionList.size() - valueList.size();
            if (difference == 0 || difference == 1) {
                for (int i = 0; i < captionList.size() && i < valueList.size(); i++) {
                    String localCaption = captionList.get(i);
                    if (localCaption.startsWith("-") || localCaption.startsWith("*") || localCaption.startsWith("•")) {
                        localCaption = captionList.get(0) + localCaption.substring(1);
                    }
                    String loweredLocalCaption = localCaption.toLowerCase();
                    loweredLocalCaption = StringHelper.readBeforeAnyOf(loweredLocalCaption, new char[]{':', '('}).trim();
                    if (!stat.containsKey(loweredLocalCaption)) {
                        stat.put(loweredLocalCaption, 0);
                    }
                    Integer captionCount = stat.get(loweredLocalCaption);
                    stat.put(loweredLocalCaption, ++captionCount);
                    String localValue = null;
                    if (difference == 1) {
                        if (i > 0) {
                            localValue = valueList.get(i - 1);
                        }
                    } else {
                        localValue = valueList.get(i);
                    }
                    if (localValue != null) {
                        localValue = StringHelper.readBeforeAnyOf(localValue, new char[]{'(', '[', '|'}).trim();
                        normalizedRows.put(loweredLocalCaption, localValue);
                    }
//                        processCommonCaptions(normInfobox, localCaption, localValue);
                }
            }
        }
            /*
            if (normInfobox.getName() != null) {
                em.merge(normInfobox);
            } else {
                logger.info("name of " + normInfobox.getName() + " is null!");
            }*/
        return normalizedRows;
    }

    private Document parseDocumentFromText(String rawText) {
        Document d = null;
        try {
            d = DocumentHelper.parseText(rawText);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return d;
    }

    @Override
    public int normalizeCities(int skipCount, int takeCount) {
        List<RawCity> raws = storageService.getRawListWhereRawNotEmpty(RawCity.class, skipCount, takeCount);
        int affectedRows = 0;

        for (RawCity raw : raws) {
            logger.info("raw text: " + raw.getText());
            NormalizedCity normInfobox = storageService.getNormByName(raw.getText(), NormalizedCity.class);
            if (normInfobox == null) {
                normInfobox = new NormalizedCity();
            }

            String rawText = raw.getRaw();
            Document d = parseDocumentFromText(rawText);

            populateType(d, normInfobox);
            if (normInfobox.getType() == null || normInfobox.getType().length() > 100) {
                continue;
            }

            normInfobox.setName(raw.getText());
            List<Node> rows = d.selectNodes("//info/row");
            int locationLevel = 1;
            List<String> allowedRegions = Arrays.asList(new RegionHandler().getPossibleCaptions());
            for (Node rowNode : rows) {
                String caption = rowNode.selectSingleNode("caption").getText().toLowerCase();
                String value = rowNode.selectSingleNode("value").getText().trim();

                processCommonCaptions(normInfobox, caption, value);
                if (caption.contains("координаты")) {
                    try {
                        URL coordinatesUrl = new URL("http:" + value);
                        String query = coordinatesUrl.getQuery();
                        String coordinates = query.substring(query.indexOf("ll=") + 3, query.indexOf("&"));
                        List<String> coordList = Splitter.on(',').splitToList(coordinates);
                        if (coordList.size() == 2) {
                            normInfobox.setLatitude(coordList.get(0));
                            normInfobox.setLongitude(coordList.get(1));
                        }
                    } catch (MalformedURLException e) {
                        logger.error(e);
                    }
                }
                if (allowedRegions.contains(caption)) {
                    value = StringHelper.readBeforeAnyOf(value, new char[]{'(', '[', ']', '|', '/'}).trim();
                    // todo если название второго региона совпадает со страной,
                    // todo это ведёт к зацикливанию на сайте (Панама ссылается на саму себя)
                    if (value.length() > 0) {
                        if (locationLevel == 1) {
                            normInfobox.setCountry(value);
                        }
                        if (locationLevel == 2) {
                            normInfobox.setSecondLocation(value);
                        }
                        if (locationLevel == 3) {
                            normInfobox.setThirdLocation(value);
                        }
                    }
                    locationLevel++;

                    // todo добавить обработку часового пояса
                }
            }

            RawRegion region = storageService.getRegionByCapitalUrl(raw.getUrl());
            if (region != null) {
                normInfobox.setCapitalOf(StringHelper.readBeforeOpenBracket(region.getText()));
            }
            region = storageService.getRegionByAdministrativeCenterUrl(raw.getUrl());
            if (region != null) {
                normInfobox.setAdministrativeCenterOf(StringHelper.readBeforeOpenBracket(region.getText()));
            }

            if (normInfobox.getCountry() != null && normInfobox.getName() != null) {
                storageService.merge(normInfobox);
                affectedRows++;
            } else {
                logger.info("name of " + normInfobox.getName() + " is null!");
            }
        }
        return affectedRows;
    }

    private void processCommonCaptions(Norm normInfobox, String caption, String value) {
        value = StringHelper.readBeforeAnyOf(value, new char[]{'['}).trim();
        if (caption.contains("основан")) {
            if (!value.contains("век") && !value.contains("в.")) {
                // todo может быть год в конце
                // todo может быть число и месяц прописью
                // todo обработка до н.э.
                if(value.matches("\\d{1,2}\\.\\d{1,2}\\.\\d{4}")) {
                    List<String> dateParts = Splitter.on(".").omitEmptyStrings().trimResults().splitToList(value);
                    if(dateParts.size() == 3) {
                        value = dateParts.get(2);
                    }
                }
                List<String> dateParts = Splitter.on(" ").omitEmptyStrings().trimResults().splitToList(value);
                if (dateParts.size() == 3 || dateParts.size() == 4) {
                    int i = 0;
                    for (; i < dateParts.size(); i++) {
                        String datePart = dateParts.get(i);
                        if (datePart.matches("[а-я]+$")) {
                            // доходим до первой строки, которая содержит только буквы
                            // скорее всего, это - название месяца
                            break;
                        }
                    }
                    if (i < dateParts.size() - 1) {
                        i++;
                        value = dateParts.get(i);
                    }
                }
                String number = StringHelper.getNumberFromString(value);
                if (CharMatcher.DIGIT.matchesAllOf(number)) {
                    int parsedNumber = Integer.parseInt(number);
                    if (parsedNumber > 0) {
                        normInfobox.setFoundationYear(parsedNumber);
                    }
                }
            }
        }

        value = StringHelper.readBeforeAnyOf(value, new char[]{'(', '|', '/'});
        // todo контроль за длиной числовой строки
        if (caption.equals("площадь")) {
            String number = StringHelper.getNumberFromString(value);
            if (CharMatcher.DIGIT.matchesAllOf(number)) {
                int parsedNumber = Integer.parseInt(number);
                if (parsedNumber > 0) {
                    normInfobox.setArea(parsedNumber);
                }
            }
        }
        if (caption.equals("плотность")) {
            String number = StringHelper.getNumberFromString(value);
            if (CharMatcher.DIGIT.matchesAllOf(number)) {
                int parsedNumber = Integer.parseInt(number);
                if (parsedNumber > 0) {
                    normInfobox.setDensity(parsedNumber);
                }
            }
        }
        if (caption.equals("население")) {
            String number = StringHelper.getNumberFromString(value);
            if (CharMatcher.DIGIT.matchesAllOf(number) && number.length() < 10) {
                int parsedNumber = Integer.parseInt(number);
                if (parsedNumber > 0) {
                    normInfobox.setPopulation(parsedNumber);
                }
            }
        }
    }

    private void populateType(Document d, NormalizedCity norm) {
        String type = d.selectSingleNode("//info").valueOf("@type");
        if (type == null) {
            type = "";
        } else {
            type = type.toLowerCase();
        }

        if (CharMatcher.inRange('а', 'я').matchesAnyOf(type)) {
            norm.setType(type);
        } else {
            logger.info("wrong type: " + norm.getType());
        }
    }

}
