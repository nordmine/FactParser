package ru.nordmine.services;

import com.google.common.base.CharMatcher;
import org.apache.log4j.Logger;
import org.dom4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.nordmine.entities.raw.Raw;
import ru.nordmine.entities.raw.RawCity;
import ru.nordmine.entities.raw.RawRegion;
import ru.nordmine.services.old.ParserServiceImpl;
import ru.nordmine.storage.StorageService;
import ru.nordmine.text.generator.handler.RegionHandler;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service("crawlerService")
public class CrawlerServiceImpl implements CrawlerService {

    private static Logger logger = Logger.getLogger(CrawlerServiceImpl.class);

    @Autowired
    private StorageService storageService;

    @Override
    public String crawlCitiesFromPage(String startListUrl) {
        String nextPage = null;
        if (startListUrl.length() == 0) {
            startListUrl = getStartListUrl();
        }
        logger.info("try parsing " + startListUrl);
        Document document = null;
        try {
            document = ParserServiceImpl.parse(startListUrl);
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        if (document != null) {
            List<String> allowedRegions = Arrays.asList(new RegionHandler().getPossibleCaptions());
            Node contentNode = document.selectSingleNode("//DIV[@id='mw-content-text']");
            List<Node> navNode = contentNode.selectNodes("//A[text() = 'следующие 200']");
            if (!navNode.isEmpty()) {
                nextPage = "http://ru.wikipedia.org" + navNode.get(0).valueOf("@href");
                logger.info("parsed next page: " + nextPage);
            }
            List<Node> pages = contentNode.selectNodes("//TABLE/TBODY/TR/TD/UL/LI/A");
            int counter = 0;
            for (Node page : pages) {
                String cityName = page.getText();
                if (storageService.getRawByName(cityName, RawCity.class) != null) {
                    logger.info("infobox for " + cityName + " already exists");
                    continue;
                }

                String url = page.valueOf("@href");
                logger.info(page.getText() + ": " + url);

                Document pageDoc = loadPageAsXml(url);
                if (pageDoc == null) {
                    continue;
                }

                RawCity raw = storageService.getRawByUrl(url, RawCity.class);
                if (raw == null) {
                    raw = new RawCity();
                }
                Document rawDocument = convertInfoboxToXml(allowedRegions, pageDoc, raw);
                Node pageHeaderNode = pageDoc.selectSingleNode("//H1");
                if (pageHeaderNode != null) {
                    raw.setText(pageHeaderNode.getStringValue());
                } else {
                    raw.setText(page.getText());
                }
                raw.setUrl(url);
                if (rawDocument != null) {
                    raw.setRaw(rawDocument.asXML());
                }
                raw.setLastUpdate(new Date());
                storageService.merge(raw);
                counter++;
                if (counter == 200) {
                    break;
                }
            }
        }
        return nextPage;
    }

    private String getStartListUrl() {
        String latestCityName = "А-Бола";
        RawCity latestCity = storageService.getLatestRawCity();
        if (latestCity != null) {
            latestCityName = latestCity.getText();
        }
        try {
            latestCityName = URLEncoder.encode(latestCityName, "UTF-8")
                    .replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            logger.error(e);
        }
        return "http://ru.wikipedia.org/w/index.php?title=%D0%9A%D0%B0%D1%82%D0%B5%D0%B3%D0%BE%D1%80%D0%B8%D1%8F:%D0%9D%D0%B0%D1%81%D0%B5%D0%BB%D1%91%D0%BD%D0%BD%D1%8B%D0%B5_%D0%BF%D1%83%D0%BD%D0%BA%D1%82%D1%8B_%D0%BF%D0%BE_%D0%B0%D0%BB%D1%84%D0%B0%D0%B2%D0%B8%D1%82%D1%83&pagefrom=" + latestCityName;
    }

    private Document loadPageAsXml(String url) {
        Document pageDoc = null;
        try {
            pageDoc = ParserServiceImpl.parse("http://ru.wikipedia.org" + url);
        } catch (DocumentException e) {
            logger.error(e);
        }
        return pageDoc;
    }

    // allowedRegions - при парсинге регионов должен быть установлен в null
    private Document convertInfoboxToXml(List<String> allowedRegions, Document document, Raw rawEntity) {
        Document rawDocument = null;

        if (document != null) {
            List<Node> infoboxNodes = document.selectNodes("//TABLE[contains(@class, 'infobox')]");
            if (infoboxNodes.isEmpty()) {
                infoboxNodes = document.selectNodes("//TABLE[contains(@style, 'float:right') or contains(@style, 'float: right')]");
            }
            if (!infoboxNodes.isEmpty()) {
                Node infoboxNode = infoboxNodes.get(0);
                List<Node> bodyNodes = infoboxNode.selectNodes("TBODY[1]");
                Node bodyNode = bodyNodes.get(0);
                rawDocument = DocumentHelper.createDocument();
                Element infoElement = rawDocument.addElement("info");
                Node objectTypeNode;
                if (allowedRegions == null) {
                    objectTypeNode = infoboxNode.selectSingleNode("CAPTION/node()[1]");
                } else {
                    objectTypeNode = bodyNode.selectSingleNode("TR/TD[1]/node()");
                }
                if (objectTypeNode != null) {
                    infoElement.addAttribute("type", CharMatcher.WHITESPACE.trimFrom(objectTypeNode.getText()));
                }
                List<Node> rows;
                if (allowedRegions == null) {
                    rows = bodyNode.selectNodes("TR");
                } else {
                    rows = bodyNode.selectNodes("TR/TD/TABLE/TBODY/TR");
                }

                int regionLevelCounter = 1;

                for (Node row : rows) {
                    List<Node> rowNodes = row.selectNodes("TD");
                    // если столбец надписей верстается как TH
                    if (rowNodes.size() == 1) {
                        List<Node> thNodes = row.selectNodes("TH[1]");
                        if (thNodes.size() == 1) {
                            rowNodes.add(0, thNodes.get(0));
                        }
                    }
                    if (rowNodes.size() == 2) {
                        Element rowElement = infoElement.addElement("row");
                        Element captionElement = rowElement.addElement("caption");
                        String caption = CharMatcher.WHITESPACE.trimFrom(rowNodes.get(0).getStringValue());
                        captionElement.setText(caption);
                        Element valueElement = rowElement.addElement("value");
                        // добавление ссылок на страницы регионов,
                        // если заданы разрешённые типы регионов
                        if (allowedRegions != null && allowedRegions.contains(caption.toLowerCase()) && regionLevelCounter <= 2) {
                            String regionNameFromHeader = saveRegion(rowNodes);
                            if (regionNameFromHeader != null) {
                                valueElement.setText(regionNameFromHeader);
                            } else {
                                populateValueFromHiddenText(rowNodes, valueElement);
                            }
                            regionLevelCounter++;
                        } else if (caption.equalsIgnoreCase("координаты")) {
                            List<Node> googleMapsNodes = rowNodes.get(1).selectNodes("node()//A");
                            for (Node anchorNode : googleMapsNodes) {
                                if (anchorNode.getText().contains("(G)")) {
                                    valueElement.setText(anchorNode.valueOf("@href"));
                                }
                            }
                        } else if (allowedRegions == null && caption.equalsIgnoreCase("столица")) {
                            List<Node> capitalNodes = rowNodes.get(1).selectNodes("node()//A[1]");
                            if (!capitalNodes.isEmpty()) {
                                RawRegion rawRegion = (RawRegion) rawEntity;
                                String cityUrl = capitalNodes.get(0).valueOf("@href");
                                if (cityUrl != null && cityUrl.startsWith("/wiki/")) {
                                    rawRegion.setCapitalUrl(cityUrl);
                                }
                            }
                            populateValueFromHiddenText(rowNodes, valueElement);
                        } else if (allowedRegions == null &&
                                (caption.equalsIgnoreCase("административный центр")
                                        || caption.equalsIgnoreCase("административный центр"))) {
                            List<Node> admCenterNodes = rowNodes.get(1).selectNodes("node()//A[1]");
                            if (!admCenterNodes.isEmpty()) {
                                RawRegion rawRegion = (RawRegion) rawEntity;
                                String cityUrl = admCenterNodes.get(0).valueOf("@href");
                                if (cityUrl != null && cityUrl.startsWith("/wiki/")) {
                                    rawRegion.setAdministrativeCenterUrl(cityUrl);
                                }
                            }
                            populateValueFromHiddenText(rowNodes, valueElement);
                        } else {
                            populateValueFromHiddenText(rowNodes, valueElement);
                        }
                    }
                }
            } else {
                logger.info("infobox not found");
            }
        }
        return rawDocument;
    }

    private void populateValueFromHiddenText(List<Node> rowNodes, Element valueElement) {
        List<Node> hiddenTextNodes = rowNodes.get(1).selectNodes("DIV/SPAN[@class='adr']/SPAN");
        if (hiddenTextNodes.isEmpty()) {
            valueElement.setText(rowNodes.get(1).getStringValue());
        } else {
            valueElement.setText(hiddenTextNodes.get(0).getStringValue());
        }
    }

    // загружает страницу с регионом и сохраняет информацию о нём в БД
    private String saveRegion(List<Node> rowNodes) {
        List<Node> regionNodes = rowNodes.get(1).selectNodes("DIV/A[position() = 1 and not(contains(@title, '(страница отсутствует)'))]");
        logger.info("region nodes count: " + regionNodes.size());
        String pageHeader = null;
        if (!regionNodes.isEmpty()) {
            Node regionNode = regionNodes.get(0);
            if (regionNode != null) {
                String url = regionNode.valueOf("@href");
                RawRegion rawRegion = storageService.getRawByUrl(url, RawRegion.class);
                logger.info("region url: " + url);
                if (rawRegion == null) {
                    Document document = loadPageAsXml(url);
                    if (document != null) {
                        rawRegion = new RawRegion();
                        Document rawXml = convertInfoboxToXml(null, document, rawRegion);
                        Node pageHeaderNode = document.selectSingleNode("//H1");
                        if (pageHeaderNode != null) {
                            pageHeader = pageHeaderNode.getStringValue();
                            logger.info("region header: " + pageHeader);
                            rawRegion.setUrl(url);
                            rawRegion.setText(pageHeader);
                            if (rawXml != null) {
                                rawRegion.setRaw(rawXml.asXML());
                            }
                            rawRegion.setLastUpdate(new Date());
                            storageService.merge(rawRegion);
                        }
                    }
                } else {
                    pageHeader = rawRegion.getText();
                }
            }
        }
        return pageHeader;
    }

}
