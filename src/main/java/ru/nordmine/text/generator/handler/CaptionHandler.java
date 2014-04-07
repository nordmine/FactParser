package ru.nordmine.text.generator.handler;

import org.apache.log4j.Logger;
import ru.nordmine.entities.norm.NormalizedCity;
import ru.nordmine.services.MorpherService;
import ru.nordmine.storage.StorageService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class CaptionHandler {

    private static Logger logger = Logger.getLogger(CaptionHandler.class);

    protected CaptionHandler nextHandler;

    protected StorageService storageService;
    protected MorpherService morpherService;

    public StorageService getStorageService() {
        return storageService;
    }

    public void setStorageService(StorageService storageService) {
        this.storageService = storageService;
    }

    public MorpherService getMorpherService() {
        return morpherService;
    }

    public void setMorpherService(MorpherService morpherService) {
        this.morpherService = morpherService;
    }

    public CaptionHandler add(CaptionHandler handler) {
        nextHandler = handler;
        handler.setStorageService(this.storageService);
        handler.setMorpherService(this.morpherService);
        return handler;
    }

    public Map<String, List<String>> process(String caption, String value, NormalizedCity normalizedCity) {
        List<String> possibleCaptions = Arrays.asList(getPossibleCaptions());
        Map<String, List<String>> result = Collections.emptyMap();
        if(possibleCaptions.contains(caption)) {
            result = getSentences(caption, value, normalizedCity);
        }
        else if (nextHandler != null) {
            result = nextHandler.process(caption, value, normalizedCity);
        }
        return result;
    }

    public void resetState() {
        if(nextHandler != null) {
            nextHandler.resetState();
        }
    }

    public abstract String[] getPossibleCaptions();

    protected abstract Map<String, List<String>> getSentences(String caption, String value, NormalizedCity normalizedCity);

}
