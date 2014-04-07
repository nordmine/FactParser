package ru.nordmine.text.generator.handler.region;

import ru.nordmine.entities.morpher.MorpherForms;
import ru.nordmine.text.generator.SentencePool;

import java.util.List;
import java.util.Map;

public class GovernorHandler extends RegionCaptionHandler {

    private static SentencePool sentences = new SentencePool(new String[]{
            "Должность value занимает ",
            "На посту value состоит ",
            "В качестве value работает ",
            "Кресло value занимает "
    });

    @Override
    public String[] getPossibleCaptions() {
        return new String[]{"глава", "глава района", "глава администрации",
                "глава государства", "губернатор", "президент", "глава республики",
                "президент совета", "премьер-министр", "председатель правительства",
                "вице-президент", "высший руководитель", "префект", "аким",
                "аким области", "император", "королева", "король", "монарх",
                "вице-губернатор", "градоначальник"};
    }

    @Override
    protected void getSentences(String caption, String value, Map<String, List<String>> textParts, String regionName, Map<String, String> rows) {
        MorpherForms forms = morpherService.loadFormsFromStorage(caption);
        addSentenceToMap(textParts, GOVERNOR,
                sentences.nextWithValue(forms.getSingleRod()) + value);
    }
}
