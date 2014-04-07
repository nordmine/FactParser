package ru.nordmine.text.generator.handler;

import org.apache.log4j.Logger;
import ru.nordmine.StringHelper;
import ru.nordmine.common.SentenceInfo;
import ru.nordmine.entities.norm.NormalizedCity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RegionHandler extends CaptionHandler {

    private static Logger logger = Logger.getLogger(RegionHandler.class);

    private int locationSentenceIndex = 0;
    private int regionLevelCounter = 0;

    private SentenceInfo[] locationSentences = new SentenceInfo[]{
            new SentenceInfo("находится в", "П"),
            new SentenceInfo("принадлежит к", "Д"),
            new SentenceInfo("располагается в", "П"),
            new SentenceInfo("относится к", "Д")
    };

    @Override
    public void resetState() {
        this.regionLevelCounter = 0;
        super.resetState();
    }

    @Override
    public String[] getPossibleCaptions() {
        return new String[]{
                "шахрестан", "муниципальное образование", "вилайет",
                "округ гонконга", "хутор северной ирландии", "мхаре", "муниципальный район",
                "аксбридж", "городская администрация", "аул", "автономная республика",
                "городской округ", "местечко", "зона", "подляское воеводство", "ляни",
                "административный округ", "округ", "автономное сообщество",
                "сельская гмина", "бывшее село", "муниципалитет", "мухафаза",
                "слобода", "территориальное управление",
                "сельский совет", "город и муниципалитет", "штат",
                "национальная туристическая зона", "сельское поселение",
                "марз", "ном", "милан", "город", "остан", "край", "кишлак", "село",
                "дим", "поселок", "бывшая деревня", "городской совет", "медье",
                "архипелаг", "этрап", "повят", "лен", "уезд", "деревня ирландии",
                "федеральная земля", "область", "районный центр", "ил", "староство",
                "приход", "городище", "пригород", "покинутое село", "сельский район",
                "кантон", "фьорд", "поселковая администрация", "сельсовет", "коммуна",
                "автономный край", "эмират", "историческая область", "комарка", "остров",
                "городской район", "автономный район", "традиционное графство",
                "управление", "аульный округ", "городское поселение", "станица", "аил",
                "фрегезия", "курорт", "станция", "боро", "поселение", "среднее село",
                "аал", "графство", "муниципия", "крупное село", "префектура",
                "поселковый совет", "хутор", "сельский округ", "энтитет",
                "регион", "страна", "велаят", "земля", "периферия", "улус",
                "провинция", "воеводство", "покинутый аул", "центральный город",
                "тауншип", "местный совет", "мошав", "община", "район города",
                "департамент", "район", "волость", "разъезд", "сельская администрация",
                "субъект федерации", "наслег", "деревня"
        };
    }

    @Override
    protected Map<String, List<String>> getSentences(String caption, String value, NormalizedCity normalizedCity) {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        List<String> lines = new LinkedList<String>();
        lines.add(getLocationTemplate(caption, StringHelper.readBeforeOpenBracket(value), normalizedCity));
        map.put("region", lines);
        return map;
    }

    private String getLocationTemplate(String caption, String value, NormalizedCity normalizedCity) {
        StringBuilder template = new StringBuilder();
        template.append("theme ");

        SentenceInfo selectedSentence = null;
        String capitalOf = normalizedCity.getCapitalOf();
        String admCenterOf = normalizedCity.getAdministrativeCenterOf();

        String country = normalizedCity.getCountry();
        if (country != null) {
            country = country.trim();
        }

        String secLocation = normalizedCity.getSecondLocation();
        if (secLocation != null) {
            secLocation = secLocation.trim();
        }

        value = StringHelper.readBeforeAnyOf(value.replace(caption, ""), new char[]{'/'}).trim();
        if (capitalOf != null) {
            capitalOf = capitalOf.trim();
            if (capitalOf.equalsIgnoreCase(value)) {
                selectedSentence = new SentenceInfo("является столицей", "Р");
            }
        } else if (admCenterOf != null && admCenterOf.length() > 0) {
            admCenterOf = admCenterOf.trim();
            if (admCenterOf.equalsIgnoreCase(value)) {
                selectedSentence = new SentenceInfo("является административным центром", "Р");
            }
        }

        if (selectedSentence == null) {
            selectedSentence = locationSentences[locationSentenceIndex];
            locationSentenceIndex++;
            if (locationSentenceIndex == locationSentences.length) {
                locationSentenceIndex = 0;
            }
        }

        String captionForm = morpherService.loadFormsFromStorage(caption).getByCode(selectedSentence.getForm());
        if (captionForm != null) {
            template.append(selectedSentence.getSentence()).append(" ");
            if (regionLevelCounter < 2) {
                template.append(captionForm).append(" {region|")
                        .append(value);
                template.append("}");
            } else {
                template.append(captionForm).append(" ")
                        .append(value);
            }
            regionLevelCounter++;
        }
        return template.toString();
    }
}
