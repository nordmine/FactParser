package ru.nordmine.services;

import ru.nordmine.entities.raw.RawRegion;

import java.util.Map;

public interface NormalizationService {

    int normalizeCities(int skipCount, int takeCount);
    int normalizeRegions(int skipCount, int takeCount);
    Map<String, String> normalizeRegion(Map<String, Integer> stat, RawRegion raw);
}
