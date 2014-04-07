package ru.nordmine.services;

import java.util.Map;

public interface TextGeneratorService {

    Map<String, String> createArticleForCity(String cityName);
    Map<String, String> createArticleForRegion(String regionName);
}
