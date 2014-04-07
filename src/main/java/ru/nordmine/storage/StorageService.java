package ru.nordmine.storage;

import ru.nordmine.entities.norm.Norm;
import ru.nordmine.entities.norm.NormalizedCity;
import ru.nordmine.entities.raw.Raw;
import ru.nordmine.entities.raw.RawCity;
import ru.nordmine.entities.raw.RawRegion;

import java.util.List;

public interface StorageService {
    // single entity
    <T extends Raw> T getRawByUrl(String url, Class<T> tClass);
    <T extends Raw> T getRawByName(String name, Class<T> tClass);
    <T extends Raw> T getRawByNameWhereRawNotEmpty(String name, Class<T> tClass);
    <T extends Norm> T getNormByName(String name, Class<T> tClass);

    // list of entities
    <T extends Raw> List<T> getRawListWhereRawNotEmpty(Class<T> tClass, int skipCount, int takeCount);
    List<NormalizedCity> getNormListByNotNameAndCountry(String countryName, String name);
    List<NormalizedCity> getNormListByNotNameAndTypeAndFoundationYear(String type, String name, Integer foundationYear);
    List<NormalizedCity> getNormListByCountryAndType(String countryName, String type);

    RawRegion getRegionByCapitalUrl(String url);

    RawRegion getRegionByAdministrativeCenterUrl(String url);

    List<NormalizedCity> getNormList(int skipCount, int takeCount);
    List<String> getAllCityNames();

    // agregation
    Integer agregateNormByNameAndCountry(String function, String fieldName, String countryName, String name);
    Integer agregateNormByNameAndRegion(String function, String fieldName, String countryName, String name, String regionName);

    RawCity getLatestRawCity();

    // saving
    void merge(Object entity);
}
