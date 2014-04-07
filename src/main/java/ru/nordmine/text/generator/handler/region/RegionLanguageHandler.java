package ru.nordmine.text.generator.handler.region;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import ru.nordmine.text.generator.SentencePool;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RegionLanguageHandler extends RegionCaptionHandler {

    private static SentencePool sentences = new SentencePool(new String[] {
            "Местные жители говорят на ",
            "Коренные жители разговаривают на ",
            "Между собой местные жители общаются на ",
            "Общение между жителями происходит на ",
            "Вы можете пообщаться с местными жителями на "
    });

    @Override
    public String[] getPossibleCaptions() {
        return new String[]{"официальные языки", "официальный язык",
                "государственный язык", "язык"};
    }

    @Override
    protected void getSentences(String caption, String value, Map<String, List<String>> textParts, String regionName, Map<String, String> rows) {
        List<String> langs = Splitter.on(",").omitEmptyStrings().splitToList(value.toLowerCase());
        List<String> correctedLangs = new LinkedList<String>();
        for (String lang : langs) {
            if(lang.matches("[а-я ]+$")) { // todo вынести в процедуру нормализации
                correctedLangs.add(morpherService.loadFormsFromStorage(lang).getSinglePred());
            }
        }
        System.out.println(textParts);
        addSentenceToMap(textParts, MISC, sentences.next() + Joiner.on(", ").join(correctedLangs)
                + (correctedLangs.size() > 1 ? " языках" : " языке"));
    }
}
