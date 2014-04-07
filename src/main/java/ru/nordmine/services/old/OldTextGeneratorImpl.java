package ru.nordmine.services.old;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import ru.nordmine.StringHelper;
import ru.nordmine.entities.raw.RawCity;
import ru.nordmine.entities.wiki.RelationValues;
import ru.nordmine.entities.wiki.WikiRelation;
import ru.nordmine.entities.wiki.WikiValue;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class OldTextGeneratorImpl {

    private static Logger logger = Logger.getLogger(OldTextGeneratorImpl.class);

    @PersistenceContext
    private EntityManager em;

    public String createTextForCountry(String countryName) {
//        StringBuilder result = new StringBuilder();
        List<WikiRelation> wikiRelations = em.createQuery("from WikiRelation wr where wr in (select rv.relation from RelationValues rv where rv.value = (select v from WikiValue v where v.text = :countryName))", WikiRelation.class)
                .setParameter("countryName", countryName)
                .getResultList();

        WikiRelation countryWikiRelation = em.createQuery("from WikiRelation wr where wr.value.text = :countryName", WikiRelation.class)
                .setParameter("countryName", countryName)
                .getSingleResult();

        wikiRelations.add(countryWikiRelation);

        String[] adverbs = new String[]{"примерно ", "приблизительно ", "около ", "почти "};
        int adverbIndex = 0;

        Random random = new Random();

        Document document = null;
        SAXReader reader = new SAXReader();
        try {
            document = reader.read("/home/boris/wiki/country_forms.xml");
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        Map<String, String> allMorphs = new HashMap<String, String>();
        for (WikiRelation wr : wikiRelations) {
            if (!wr.getHeader().isDigital() && !CharMatcher.inRange('a', 'z').matchesAnyOf(wr.getValue().getText())) {
                logger.info(wr.getValue().getText());
                Map<String, String> morph = getMorph(document, wr.getHeader().getText(), wr.getValue().getText());
                allMorphs.putAll(morph);
                String headerText = wr.getHeader().getText();
                // получение страны, в которой есть совпадение по языку или валюте случайным образом
                if (headerText.equals("валюта") || headerText.equals("язык")) {
                    List<WikiValue> otherValues = em.createQuery("select rv.value from RelationValues rv where rv.relation.value = :relationValue and rv.value.text != :countryName", WikiValue.class)
                            .setParameter("relationValue", wr.getValue())
                            .setParameter("countryName", countryName)
                            .getResultList();
                    if (!otherValues.isEmpty()) {
                        String otherValue = otherValues.get(random.nextInt(otherValues.size())).getText();
                        allMorphs.putAll(getMorph(document, headerText + "2", otherValue));
                    }
                }
            }
            // выполнение статистических расчётов для числовых данных
            if (wr.getHeader().isDigital()) {
                BigDecimal b = new BigDecimal(wr.getValue().getText());
                String value = b.toString();
                b = b.setScale(0, RoundingMode.HALF_UP);

                // деление по тысяче для увеличения читаемости
                BigDecimal[] divideAndRemainder = b.divideAndRemainder(new BigDecimal("1000"));
                logger.info("remainder: " + divideAndRemainder[1].toString());
                if (divideAndRemainder[0].compareTo(new BigDecimal("10")) == 1) {
                    b = divideAndRemainder[0];
                    value = b.toString() + " тыс.";
                }
                divideAndRemainder = b.divideAndRemainder(new BigDecimal("1000"));
                logger.info("remainder: " + divideAndRemainder[1].toString());
                if (divideAndRemainder[0].compareTo(new BigDecimal("10")) == 1) {
                    b = divideAndRemainder[0];
                    value = b.toString() + " млн.";
                }
                allMorphs.put("[" + wr.getHeader().getText() + "]", adverbs[adverbIndex] + value);
                adverbIndex++;
                if (adverbIndex == adverbs.length) {
                    adverbIndex = 0;
                }
                // -----------------------


                List<RelationValues> otherValues = em.createQuery("select rv from RelationValues rv where rv.relation.header = :header and rv.value.text != :countryName", RelationValues.class)
                        .setParameter("header", wr.getHeader())
                        .setParameter("countryName", countryName)
                        .getResultList();
                Map<String, String> stat = new HashMap<String, String>();
                BigDecimal current = new BigDecimal(wr.getValue().getText());
                for (RelationValues rv : otherValues) {
                    BigDecimal bd = new BigDecimal(rv.getRelation().getValue().getText()).setScale(0, RoundingMode.HALF_UP);
                    if (bd.compareTo(BigDecimal.ZERO) == 0) {
                        continue;
                    }
                    String otherCountryName = rv.getValue().getText();
                    BigDecimal scale = current.divideToIntegralValue(bd).setScale(0, RoundingMode.HALF_UP);
                    logger.info(otherCountryName + " * " + scale + " = " + countryName);
                    if (scale.compareTo(BigDecimal.ONE) == 1 && scale.compareTo(BigDecimal.TEN) <= 0) {
                        stat.put(scale.toString(), otherCountryName);
                    }
                }
                for (Map.Entry<String, String> entry : stat.entrySet()) {
                    allMorphs.putAll(getMorph(document, wr.getHeader().getText() + "2", entry.getValue()));
                    allMorphs.put("[" + wr.getHeader().getText() + "3]", entry.getKey());
                    break;
                }
            }
//            result.append(wr.getHeader().getText()).append(": ").append(wr.getValue().getText()).append("\n");
        }

        String pattern = Joiner.on(". ").join(getPatterns()) + ".";

        for (Map.Entry<String, String> entry : allMorphs.entrySet()) {
            logger.info(entry.getKey());
            pattern = pattern.replace(entry.getKey(), entry.getValue());
        }

        return pattern;
    }


    private List<String> getPatterns() {
        List<String> patterns = new ArrayList<String>();
        patterns.add("Столицей [страна:Р] является город [столица]");
        patterns.add("Население [полное наименование:Р] составляет [население] человек, что превышает население [население2:Р] в [население3] раз");
        patterns.add("В качестве валюты, как и в [валюта2:П], используется [валюта]");
        patterns.add("Ежегодно [страна:В] посещают [туристы, млн] миллионов туристов");
        patterns.add("Наиболее высокой точкой [страна:Р] является [наивысшая точка]");
        patterns.add("Площадь [полное наименование:Р] составляет [площадь] квадратных километров. Это в [площадь3] раз больше, чем площадь [площадь2:Р]");
        patterns.add("Жители [страна:Р] говорят на [язык:П] языке. На этом же языке говорят и в [язык2:П]");
        patterns.add("Потребление пива в [страна:Д] составляет [потребление пива, л спирта] литров чистого спирта в год");
        patterns.add("Длина автомобильных дорог в [страна:Д] составляет [длина автодорог, км] километров. Для сравнения, в [длина автодорог, км2:П] этот показатель меньше в [длина автодорог, км3] раза");
        patterns.add("На каждую тысячу человек [полное наименование:Р] приходится [кол-во авто] машин");
        return patterns;
    }

    private Map<String, String> getMorph(Document document, String header, String value) {
        Map<String, String> morph = new HashMap<String, String>();
        List<Node> nodeList = document.selectSingleNode("//morph").selectNodes("node[@name='" + value + "']/node()");
        logger.info("node list size: " + nodeList.size());
        for (Node node : nodeList) {
            if (node.getName() != null) {
                morph.put("[" + header + ":" + node.getName() + "]", node.getText());
            }
        }
        morph.put("[" + header + "]", StringHelper.upperFirstChar(value));
        return morph;
    }

    public void groupCitiesByType(String countryName) {
        List<RawCity> rawItems = em.createQuery("from RawCity r", RawCity.class)
                .getResultList();
        Map<String, List<String>> cities = new HashMap<String, List<String>>();
        for (RawCity rawItem : rawItems) {
            Document d = null;
            try {
                d = DocumentHelper.parseText(rawItem.getRaw());
            } catch (DocumentException e) {
                e.printStackTrace();
            }
            if (d != null) {
                List<Node> rows = d.selectNodes("//info/row");
                String type = d.selectSingleNode("//info").valueOf("@type");
                for (Node row : rows) {
                    String caption = row.selectSingleNode("caption").getText().toLowerCase();
                    if (caption.equalsIgnoreCase("страна")) {
                        String value = row.selectSingleNode("value").getText();
                        if (value.equals(countryName)) {
                            if (!cities.containsKey(type)) {
                                cities.put(type, new ArrayList<String>());
                            }
                            cities.get(type).add(rawItem.getText());
                        }
                    }
                }
            }
        }
        logger.info("cities: " + cities);
    }
}
