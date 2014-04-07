package ru.nordmine.services.old;

import com.google.common.base.CharMatcher;
import org.apache.log4j.Logger;
import org.cyberneko.html.parsers.DOMParser;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.DOMReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;
import ru.nordmine.Program;
import ru.nordmine.common.AnchorInfo;
import ru.nordmine.entities.wiki.RelationValues;
import ru.nordmine.entities.wiki.WikiHeader;
import ru.nordmine.entities.wiki.WikiRelation;
import ru.nordmine.entities.wiki.WikiValue;
import ru.nordmine.parser.ColumnInfo;
import ru.nordmine.parser.PageInfo;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

@Service("parserService")
public class ParserServiceImpl implements ParserService {

    private static Logger logger = Logger.getLogger(ParserServiceImpl.class);

    @PersistenceContext
    private EntityManager em;

    private List<ColumnInfo> columns;
    private List<WikiHeader> headersByColumn;
    private List<Node> tableHeaders;
    private Set<Integer> indexSet;


    @Override
    @Transactional
    public void normalizeData() {
        List<PageInfo> pages = configurePages();

        for (PageInfo page : pages) {
            try {
                Document doc = parse(page.getAddress());
                parse(doc, page);
            } catch (DocumentException e) {
                logger.error(e);
            }
        }
    }

    public static Document parse(String url) throws DocumentException {
        DOMParser parser = new DOMParser();
        try {
            parser.parse(url);
        } catch (SAXException e) {
            logger.error(e);
        } catch (IOException e) {
            logger.error(e);
        }
        DOMReader reader = new DOMReader();
        org.dom4j.Document doc = reader.read(parser.getDocument());
        return doc;
    }

    public void parse(Document document, PageInfo pageInfo) throws DocumentException {
        columns = pageInfo.getColumns();
        List<Node> tableList = document.selectNodes(pageInfo.getTableXPath());
        logger.info("table count = " + tableList.size());

        for (Node tableNode : tableList) {
            // --- обработка таблицы ---
            tableHeaders = tableNode.selectNodes(pageInfo.getHeaderXPath());
            logger.info("rows count: " + tableHeaders.size());

            headersByColumn = new ArrayList<WikiHeader>(tableHeaders.size());
            indexSet = new HashSet<Integer>();

            processTableHeaders();
            processTableRows(pageInfo, tableNode);
        }
    }

    private void processTableRows(PageInfo pageInfo, Node tableNode) {
        List<Node> tableRows = tableNode.selectNodes(pageInfo.getRowXPath());
        next_cell:
        for (Node tableRow : tableRows) {
            // --- обработка строки таблицы ---
            List<Node> cells = tableRow.selectNodes("TD");
            if (cells.isEmpty()) {
                continue;
            }
            int selectedHeadersIndex = 0;
            WikiValue primaryValue = null;

            List<RelationValues> relationValuesList = new ArrayList<RelationValues>();

            for (int i = 0; i < cells.size(); i++) {
                // --- обработка ячейки ---
                if (!indexSet.contains(i)) {
                    continue;
                }

                Node cell = cells.get(i);
                List<AnchorInfo> selectedValues = new ArrayList<AnchorInfo>();

                ColumnInfo columnInfo = columns.get(selectedHeadersIndex);
                boolean isNumber = columnInfo.isDigital();
                if (isNumber) {
                    if (columnInfo.getXpath() != null) {
                        selectedValues = filterNumericValues(cell.selectNodes(columnInfo.getXpath()));
                    }
                    if (selectedValues.isEmpty()) {
                        selectedValues = filterNumericValues(cell.selectNodes("node()"));
                    }
                } else {
                    if (columnInfo.getXpath() != null) {
                        selectedValues = filterTextValues(cell.selectNodes(columnInfo.getXpath()));
                    }
                    if (selectedValues.isEmpty()) {
                        selectedValues = filterTextValues(cell.selectNodes("node()/A"));
                    }
                    if (selectedValues.isEmpty()) {
                        selectedValues = filterTextValues(cell.selectNodes("node()"));
                    }
                }

                if(selectedValues.isEmpty()) {
                    selectedHeadersIndex++;
                    continue;
                }

                List<WikiValue> wikiValues = new ArrayList<WikiValue>();
                if (isNumber) {
                    for (AnchorInfo anchorInfo : selectedValues) {
                        WikiValue v = new WikiValue();
                        wikiValues.add(v);
                        v.setText(anchorInfo.getTitle());
                        em.persist(v);
                    }
                } else {
                    Map<String, String> wikiValueAddressList = new HashMap<String, String>();
                    for (AnchorInfo info : selectedValues) {
                        if (info.getAddress() != null && !info.getAddress().isEmpty()) {
                            wikiValueAddressList.put(info.getAddress(), info.getTitle());
                        }
                    }

                    logger.info(selectedValues);
                    wikiValues = em.createQuery("from WikiValue w where w.url in (:value_list)", WikiValue.class)
                            .setParameter("value_list", wikiValueAddressList.keySet())
                            .getResultList();
                    logger.info("wikiValues size: " + wikiValues.size());

                    if (wikiValueAddressList.size() > wikiValues.size()) {
                        if (columnInfo.isEnableCreate()) {
                            next_val:
                            for (Map.Entry<String, String> selectedValue : wikiValueAddressList.entrySet()) {
                                for (WikiValue wv : wikiValues) {
                                    if (wv.getUrl().equalsIgnoreCase(selectedValue.getKey())) {
                                        continue next_val;
                                    }
                                }
                                WikiValue v = new WikiValue();
                                wikiValues.add(v);
                                v.setText(selectedValue.getValue());
                                v.setUrl(selectedValue.getKey());
                                em.persist(v);
                            }
                        } else {
                            continue next_cell;
                        }
                    }
                }

                List<WikiRelation> relations = mergeWikiRelations(selectedHeadersIndex, wikiValues);

                // если текущая колонка является первичной
                if (pageInfo.getPrimaryField().equalsIgnoreCase(headersByColumn.get(selectedHeadersIndex).getText())) {
                    // в первичной колонке всегда одно значение
                    primaryValue = wikiValues.get(0);
                } else {
                    for (WikiRelation relation : relations) {
                        RelationValues relationValues = new RelationValues();
                        relationValues.setRelation(relation);
                        relationValuesList.add(relationValues);
                    }
                }

                selectedHeadersIndex++;
            }

            for (RelationValues rv : relationValuesList) {
                rv.setValue(primaryValue);
                em.persist(rv);
            }
            logger.info("");
        }
        logger.info("");
    }

    private List<WikiRelation> mergeWikiRelations(int selectedHeadersIndex, List<WikiValue> wikiValues) {
        List<WikiRelation> relations = em.createQuery("from WikiRelation w where w.header = :header and w.value in (:value_list)", WikiRelation.class)
                .setParameter("header", headersByColumn.get(selectedHeadersIndex))
                .setParameter("value_list", wikiValues)
                .getResultList();

        if (wikiValues.size() > relations.size()) {
            next_rel:
            for (WikiValue v : wikiValues) {
                for(WikiRelation rel : relations)
                {
                    if(rel.getValue().getText().equalsIgnoreCase(v.getText()))
                    {
                        continue next_rel;
                    }
                }
                WikiRelation r = new WikiRelation();
                relations.add(r);
                r.setValue(v);
                r.setHeader(headersByColumn.get(selectedHeadersIndex));
                em.persist(r);
            }
        }
        return relations;
    }

    private void processTableHeaders() {
        ColumnInfo selectedColumnInfo = null;

        for (int i = 0; i < tableHeaders.size(); i++) {
            Node tableHeader = tableHeaders.get(i);
            String header = CharMatcher.WHITESPACE.trimFrom(tableHeader.getStringValue());
            header = header.replace("-\n", "");
            header = header.replace("\n", " ");
            logger.info(header);

            for (ColumnInfo columnInfo : columns) {
                if (header.contains(columnInfo.getHeaderContains())) {
                    selectedColumnInfo = columnInfo;
                    indexSet.add(i);
                }
            }
            if (selectedColumnInfo != null && header.contains(selectedColumnInfo.getHeaderContains())) {
                List<WikiHeader> wikiHeaders = em.createQuery("from WikiHeader w where w.text = :text and w.isDigital = :isDigital", WikiHeader.class)
                        .setParameter("text", selectedColumnInfo.getSaveAs())
                        .setParameter("isDigital", selectedColumnInfo.isDigital())
                        .getResultList();
                WikiHeader wikiHeader;
                if (wikiHeaders.isEmpty()) {
                    wikiHeader = new WikiHeader();
                    wikiHeader.setText(selectedColumnInfo.getSaveAs());
                    wikiHeader.setDigital(selectedColumnInfo.isDigital());
                    em.persist(wikiHeader);
                } else {
                    wikiHeader = wikiHeaders.get(0);
                }
                headersByColumn.add(wikiHeader);
            }
        }
    }

    private List<AnchorInfo> filterNumericValues(List<Node> nodes) {
        List<AnchorInfo> lines = new ArrayList<AnchorInfo>();
        for (Node node : nodes) {
            String nodeStringValue = node.getStringValue();
            AnchorInfo value = new AnchorInfo();
            if(CharMatcher.inRange('0', '9').matchesNoneOf(nodeStringValue)) {
                continue;
            }
            if(nodeStringValue.contains("(")) {
                nodeStringValue = nodeStringValue.substring(0, nodeStringValue.indexOf("("));
            }
            String val = CharMatcher.inRange('0', '9').or(CharMatcher.anyOf(".,")).retainFrom(nodeStringValue);
            val = val.replace(',', '.');
            if(val.contains(".")) {
                val = val.substring(0, val.indexOf("."));
            }
            BigInteger d = new BigInteger(val);
            value.setTitle(d.toString());
            if (value.getTitle() != null && !value.getTitle().isEmpty()) {
                lines.add(value);
            }
        }
        return lines;
    }

    private List<AnchorInfo> filterTextValues(List<Node> nodes) {
        List<AnchorInfo> lines = new ArrayList<AnchorInfo>();
        for (Node node : nodes) {
            String nodeStringValue = node.getStringValue();
            AnchorInfo value = new AnchorInfo();
            if (node.hasContent() && node.getName().equals("A")) {
                String val = node.valueOf("@title");
                if (val.contains("(")) {
                    val = val.substring(0, val.indexOf("("));
                }
                val = CharMatcher.WHITESPACE.trimFrom(val);
                value.setTitle(val);
                if (!node.valueOf("@class").equals("new")) {
                    String href = node.valueOf("@href");
                    if (href.startsWith("/wiki/")) {
                        value.setAddress(href);
                    }
                }
            } else {
                value.setTitle(CharMatcher.WHITESPACE.trimFrom(nodeStringValue));
            }
//                value = value.toLowerCase();
            if (value.getAddress() != null && !value.getAddress().isEmpty()) {
                lines.add(value);
            }
        }
        return lines;
    }

    public static List<PageInfo> configurePages() {
        List<PageInfo> pages = new ArrayList<PageInfo>();

        {
            PageInfo europePage = new PageInfo();
            europePage.setPrimaryField("страна");
            europePage.setAddress("/home/boris/wiki/europe.html");
            europePage.setTableXPath("//TABLE[@class='wikitable' and position() <= 6]");
            europePage.getColumns().add(new ColumnInfo("Название", "страна", "B"));
            europePage.getColumns().add(new ColumnInfo("Столица", "столица"));
            europePage.getColumns().add(new ColumnInfo("Язык", "язык", "A"));
            europePage.getColumns().add(new ColumnInfo("Валюта", "валюта", "node()[1]"));
            europePage.getColumns().add(new ColumnInfo("Площадь", "площадь", "node()[2]", true));
            europePage.getColumns().add(new ColumnInfo("Население", "население", "node()[2]", true));
            europePage.getColumns().add(new ColumnInfo("ВВП", "ВВП", "node()[2]", true));
            pages.add(europePage);
        }

        {
            PageInfo northAmericaPage = new PageInfo();
            northAmericaPage.setPrimaryField("страна");
            northAmericaPage.setAddress("/home/boris/wiki/north_america.html");
            northAmericaPage.setTableXPath("//TABLE[@class='wikitable' and position() <= 1]");
            northAmericaPage.getColumns().add(new ColumnInfo("Название", "страна", "B"));
            northAmericaPage.getColumns().add(new ColumnInfo("Столица", "столица"));
            northAmericaPage.getColumns().add(new ColumnInfo("Язык", "язык", "A"));
            northAmericaPage.getColumns().add(new ColumnInfo("Денежная единица", "валюта", "A"));
            northAmericaPage.getColumns().add(new ColumnInfo("Площадь", "площадь", "node()[1]", true));
            northAmericaPage.getColumns().add(new ColumnInfo("Население", "население", "node()[1]", true));
            northAmericaPage.getColumns().add(new ColumnInfo("ВВП", "ВВП", "node()[1]", true));
            pages.add(northAmericaPage);
        }

        {
            PageInfo southAmericaPage = new PageInfo();
            southAmericaPage.setPrimaryField("страна");
            southAmericaPage.setAddress("/home/boris/wiki/south_america.html");
            southAmericaPage.setTableXPath("//TABLE[@class='wikitable' and position() <= 1]");
            southAmericaPage.getColumns().add(new ColumnInfo("Название", "страна", "B"));
            southAmericaPage.getColumns().add(new ColumnInfo("Столица", "столица", "A[1]"));
            southAmericaPage.getColumns().add(new ColumnInfo("Язык", "язык", "A"));
            southAmericaPage.getColumns().add(new ColumnInfo("Денежная единица", "валюта", "node()[1]"));
            southAmericaPage.getColumns().add(new ColumnInfo("Площадь", "площадь", "node()[1]", true));
            southAmericaPage.getColumns().add(new ColumnInfo("Население", "население", "node()[1]", true));
            southAmericaPage.getColumns().add(new ColumnInfo("ВВП", "ВВП", "node()[1]", true));
            pages.add(southAmericaPage);
        }

        {
            PageInfo asiaPage = new PageInfo();
            asiaPage.setPrimaryField("страна");
            asiaPage.setAddress("/home/boris/wiki/asia.html");
            asiaPage.setTableXPath("//TABLE[@class='wikitable' and position() <= 6]");
            asiaPage.getColumns().add(new ColumnInfo("Название", "страна", "A[1]"));
            asiaPage.getColumns().add(new ColumnInfo("Столица", "столица", "A[1]"));
            asiaPage.getColumns().add(new ColumnInfo("Язык", "язык", "A"));
            asiaPage.getColumns().add(new ColumnInfo("Валюта", "валюта", "A"));
            asiaPage.getColumns().add(new ColumnInfo("Площадь", "площадь", "CENTER/node()[1]", true));
            asiaPage.getColumns().add(new ColumnInfo("Население", "население", "CENTER/node()[1]", true));
            pages.add(asiaPage);
        }

        {
            PageInfo africaPage = new PageInfo();
            africaPage.setPrimaryField("страна");
            africaPage.setAddress("/home/boris/wiki/africa.html");
            africaPage.setTableXPath("//TABLE[@class='wikitable sortable' and position() <= 1]");
            africaPage.setRowXPath("TBODY/TR[string-length(@style)=0]");
            africaPage.getColumns().add(new ColumnInfo("Название", "страна", "A[1]"));
            africaPage.getColumns().add(new ColumnInfo("Столица", "столица", "A[1]"));
            africaPage.getColumns().add(new ColumnInfo("Денежная единица", "валюта", "A"));
            africaPage.getColumns().add(new ColumnInfo("Язык", "язык", "A"));
            africaPage.getColumns().add(new ColumnInfo("Площадь", "площадь", "node()[2]", true));
            africaPage.getColumns().add(new ColumnInfo("Население", "население", "node()[2]", true));
            africaPage.getColumns().add(new ColumnInfo("ВВП", "ВВП", "node()[2]", true));
            pages.add(africaPage);
        }
/*
        {
            PageInfo classifierPage = new PageInfo();
            classifierPage.setPrimaryField("страна");
            classifierPage.setHeaderXPath("TBODY/TR[2]/TH");
            classifierPage.setAddress("/home/boris/wiki/countries.html");
            ColumnInfo countryNameColumnInfo = new ColumnInfo("Краткое", "страна", "node()[1]");
            countryNameColumnInfo.setEnableCreate(false);
            classifierPage.getColumns().add(countryNameColumnInfo);
            classifierPage.getColumns().add(new ColumnInfo("Полное", "полное наименование"));
            pages.add(classifierPage);
        }
        */
        // -------------------------------------------------------
        {
            PageInfo touristsVisitPage = new PageInfo();
            touristsVisitPage.setPrimaryField("страна");
            touristsVisitPage.setHeaderXPath("TBODY/TR/TH");
            touristsVisitPage.setAddress("/home/boris/wiki/tourists_visit.html");
            touristsVisitPage.setTableXPath("//TABLE[@class='wikitable sortable' and position() >= 2 and position() <= 6]");
            ColumnInfo countryNameColumnInfo = new ColumnInfo("Страна", "страна", "node()[1]");
            countryNameColumnInfo.setEnableCreate(false);
            touristsVisitPage.getColumns().add(countryNameColumnInfo);
            touristsVisitPage.getColumns().add(new ColumnInfo("Количество туристических прибытий", "туристы, млн", "node()[1]", true));
            pages.add(touristsVisitPage);
        }
/*
        {
            PageInfo alcoholCountPage = new PageInfo();
            alcoholCountPage.setPrimaryField("страна");
            alcoholCountPage.setHeaderXPath("TBODY/TR/TH");
            alcoholCountPage.setAddress("/home/boris/wiki/alcohol_count.html");
            alcoholCountPage.setTableXPath("//TABLE[@class='wikitable sortable' and position() <= 1]");
            ColumnInfo countryNameColumnInfo = new ColumnInfo("Страна", "страна", "SPAN/SPAN/A[1]");
            countryNameColumnInfo.setEnableCreate(false);
            alcoholCountPage.getColumns().add(countryNameColumnInfo);
            alcoholCountPage.getColumns().add(new ColumnInfo("Общее потребление", "общее потребление, л спирта", "node()[1]", true));
            alcoholCountPage.getColumns().add(new ColumnInfo("Пиво", "потребление пива, л спирта", "node()[1]", true));
            alcoholCountPage.getColumns().add(new ColumnInfo("Вино", "потребление вина, л спирта", "node()[1]", true));
            pages.add(alcoholCountPage);
        }
*/
        {
            PageInfo carCountPage = new PageInfo();
            carCountPage.setPrimaryField("страна");
            carCountPage.setHeaderXPath("TBODY/TR/TH");
            carCountPage.setAddress("/home/boris/wiki/car_count.html");
            carCountPage.setTableXPath("//TABLE[@class='wikitable sortable' and position() <= 1]");
            ColumnInfo countryNameColumnInfo = new ColumnInfo("Страна", "страна", "SPAN/SPAN/A[1]");
            countryNameColumnInfo.setEnableCreate(false);
            carCountPage.getColumns().add(countryNameColumnInfo);
            carCountPage.getColumns().add(new ColumnInfo("Авто", "кол-во авто", "node()[1]", true));
            pages.add(carCountPage);
        }
/*
        {
            PageInfo carCountPage = new PageInfo();
            carCountPage.setPrimaryField("страна");
            carCountPage.setHeaderXPath("TBODY/TR/TH");
            carCountPage.setAddress("/home/boris/wiki/car_roads.html");
            carCountPage.setTableXPath("//TABLE[@class='sortable wikitable' and position() <= 1]");
            ColumnInfo countryNameColumnInfo = new ColumnInfo("Страна", "страна", "SPAN/SPAN/A[1]");
            countryNameColumnInfo.setEnableCreate(false);
            carCountPage.getColumns().add(countryNameColumnInfo);
            carCountPage.getColumns().add(new ColumnInfo("Длина автодорог", "длина автодорог, км", "node()[1]", true));
            pages.add(carCountPage);
        }
*/
        {
            PageInfo highestPointsPage = new PageInfo();
            highestPointsPage.setPrimaryField("страна");
            highestPointsPage.setHeaderXPath("TBODY/TR[1]/TD");
            highestPointsPage.setRowXPath("TBODY/TR[position() > 1]");
            highestPointsPage.setAddress("/home/boris/wiki/highest_points.html");
            highestPointsPage.setTableXPath("//TABLE[@class='wikitable sortable' and position() <= 1]");
            ColumnInfo countryNameColumnInfo = new ColumnInfo("Страна", "страна", "SPAN/A[1]");
            countryNameColumnInfo.setEnableCreate(false);
            highestPointsPage.getColumns().add(countryNameColumnInfo);
            highestPointsPage.getColumns().add(new ColumnInfo("Название наивысшей точки", "наивысшая точка", "A[1]"));
            pages.add(highestPointsPage);
        }
        return pages;
    }

}
