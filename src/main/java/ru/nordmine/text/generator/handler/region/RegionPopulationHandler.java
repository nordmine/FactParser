package ru.nordmine.text.generator.handler.region;

import ru.nordmine.StringHelper;
import ru.nordmine.text.generator.SentencePool;

import java.util.List;
import java.util.Map;

public class RegionPopulationHandler extends RegionCaptionHandler {

    private static SentencePool sentences = new SentencePool(new String[] {
            "Население составляет value чел",
            "Здесь проживает value чел",
            "Количество проживающих здесь равняется value чел",
            "Всего по данным статистики здесь насчитывается value чел"
    });

    @Override
    public String[] getPossibleCaptions() {
        return new String[]{"население", "население ", "население оценка"};
    }

    @Override
    protected void getSentences(String caption, String value, Map<String, List<String>> textParts, String regionName, Map<String, String> rows) {
        if (value != null && value.length() > 0) {
            addSentenceToMap(textParts, STAT, sentences.nextWithValue(
					StringHelper.roundNumber(
							StringHelper.getNumberFromString(value)
					))
			);
        }
    }
}
