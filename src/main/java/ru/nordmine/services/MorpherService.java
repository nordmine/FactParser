package ru.nordmine.services;

import ru.nordmine.entities.morpher.MorpherForms;

import java.util.Set;

public interface MorpherService {

    void getWordFormsForCountries();
    void getWordFormsForInfoboxCaptions();
    void getFormsForWordList(Set<String> wordList);
    MorpherForms loadFormsFromStorage(String word);
}
