package ru.nordmine.text.generator.handler.region;

import ru.nordmine.StringHelper;
import ru.nordmine.entities.morpher.MorpherForms;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PolityHandler extends RegionCaptionHandler {

    @Override
    public String[] getPossibleCaptions() {
        return new String[] {"форма правления"};
    }

    @Override
    protected void getSentences(String caption, String value, Map<String, List<String>> textParts, String regionName, Map<String, String> rows) {
        MorpherForms forms = morpherService.loadFormsFromStorage(value.toLowerCase());
        String sentence = regionName + " является " + forms.getSingleTvor();
        String sentenceForList = forms.getSingleImen();
        if(rows.containsKey("столица")) {
            // todo хак для оттава
            String capitalName = StringHelper.readBeforeAnyOf(rows.get("столица"), new char[] {' '});
            if(allCityNames.contains(capitalName)) {
                try {
                    capitalName = "<a href=\"/city/" + URLEncoder.encode(capitalName, "utf-8").replace("+", "%20") + "\">" + capitalName + "</a>";
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            sentence += " со столицей в городе " + capitalName;
            sentenceForList += " со столицей в городе " + capitalName;
        }
        System.out.println(textParts);
        addSentenceToMap(textParts, MAIN, sentence);
        addSentenceToMap(textParts, MAIN_FOR_LIST, sentenceForList);
    }
}
