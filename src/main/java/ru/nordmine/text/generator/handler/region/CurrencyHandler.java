package ru.nordmine.text.generator.handler.region;

import ru.nordmine.entities.morpher.MorpherForms;
import ru.nordmine.text.generator.SentencePool;

import java.util.List;
import java.util.Map;

public class CurrencyHandler extends RegionCaptionHandler {

    private static SentencePool sentences = new SentencePool(new String[] {
            " для взаиморасчётов используют ",
            " в качестве валюты используют ",
            " в магазинах расплачиваются за ",
            " покупки совершают за "
    });

    @Override
    public String[] getPossibleCaptions() {
        return new String[]{"валюта"};
    }

    @Override
    protected void getSentences(String caption, String value, Map<String, List<String>> textParts, String regionName, Map<String, String> rows) {
        value = value.toLowerCase();
        MorpherForms forms = morpherService.loadFormsFromStorage(value);
        if (value.matches("[а-я ]+$")) {
            addSentenceToMap(textParts, MISC, getEthno(rows) + sentences.next()
                    + forms.getMultipleVin());
        }
    }
}
