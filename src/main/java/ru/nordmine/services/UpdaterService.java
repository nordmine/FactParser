package ru.nordmine.services;

public interface UpdaterService {

    int updateCities(int skipCount, int takeCount);
    int updateRegions(int skipCount, int takeCount);
}
