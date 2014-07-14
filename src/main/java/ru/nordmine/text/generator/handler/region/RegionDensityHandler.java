package ru.nordmine.text.generator.handler.region;

import ru.nordmine.StringHelper;
import ru.nordmine.text.generator.SentencePool;

import java.util.List;
import java.util.Map;

public class RegionDensityHandler extends RegionCaptionHandler {

    private static SentencePool sentences = new SentencePool(new String [] {
            "В среднем на один квадратный километр приходится value чел",
            "На одном квадратном километре проживает value чел",
            "На площади, равной одному квадратному километру, проживает value чел"
    });

    @Override
    public String[] getPossibleCaptions() {
        return new String[]{"плотность", "население плотность", "плотность населения"};
    }

    @Override
    protected void getSentences(String caption, String value, Map<String, List<String>> textParts, String regionName, Map<String, String> rows) {
        if (value != null) {
            addSentenceToMap(textParts, STAT, sentences.nextWithValue(
					StringHelper.roundNumber(
							StringHelper.getNumberFromString(value)
					))
			);
        }
    }
}
