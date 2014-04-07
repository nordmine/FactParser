package ru.nordmine.storage;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nordmine.Program;
import ru.nordmine.entities.norm.Norm;
import ru.nordmine.entities.norm.NormalizedCity;
import ru.nordmine.entities.raw.Raw;
import ru.nordmine.entities.raw.RawCity;
import ru.nordmine.entities.raw.RawRegion;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service("storageService")
public class StorageServiceImpl implements StorageService {

    private static Logger logger = Logger.getLogger(StorageServiceImpl.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public <T extends Raw> T getRawByUrl(String url, Class<T> tClass) {
        List<T> rawCityList = em.createQuery("from " + tClass.getName() + " where url = :url", tClass)
                .setParameter("url", url)
                .getResultList();

        if (!rawCityList.isEmpty()) {
            return rawCityList.get(0);
        }
        return null;
    }

    @Override
    public <T extends Raw> T getRawByName(String name, Class<T> tClass) {
        return getEntityByName("text", tClass, name);
    }

    @Override
    public <T extends Norm> T getNormByName(String name, Class<T> tClass) {
        return getEntityByName("name", tClass, name);
    }

    private <T> T getEntityByName(String fieldName, Class<T> tClass, String name) {
        List<T> entityList = em.createQuery("from " + tClass.getName() + " where " + fieldName + " = :name", tClass)
                .setParameter("name", name)
                .getResultList();

        if (!entityList.isEmpty()) {
            return entityList.get(0);
        }
        return null;
    }

    @Override
    public <T extends Raw> T getRawByNameWhereRawNotEmpty(String name, Class<T> tClass) {
        List<T> entityList = em.createQuery("from " + tClass.getName() + " where text = :name and raw != null", tClass)
                .setParameter("name", name)
                .getResultList();

        if (!entityList.isEmpty()) {
            return entityList.get(0);
        }
        return null;
    }

    @Override
    public <T extends Raw> List<T> getRawListWhereRawNotEmpty(Class<T> tClass, int skipCount, int takeCount) {
        return em.createQuery("from " + tClass.getName() + " where raw != null order by text asc", tClass)
                .setFirstResult(skipCount)
                .setMaxResults(takeCount)
                .getResultList();
    }

    @Override
    public List<NormalizedCity> getNormListByNotNameAndCountry(String countryName, String name) {
        return em.createQuery("from NormalizedCity n where n.country = :country and n.name != :name and n.population > :population", NormalizedCity.class)
                .setParameter("country", countryName)
                .setParameter("name", name)
                .setParameter("population", Program.MIN_POPULATION)
                .getResultList();
    }

    @Override
    public List<NormalizedCity> getNormListByCountryAndType(String countryName, String type) {
        return em.createQuery("from NormalizedCity n where n.country = :country and n.type = :type and n.population >= :population", NormalizedCity.class)
                .setParameter("country", countryName)
                .setParameter("type", type)
                .setParameter("population", Program.MIN_POPULATION)
                .getResultList();
    }

    @Override
    public List<NormalizedCity> getNormListByNotNameAndTypeAndFoundationYear(String type, String name, Integer foundationYear) {
        return em.createQuery("from NormalizedCity where type = :type and name != :name and abs(foundationYear - :foundationYear) <= 10 and population > :population", NormalizedCity.class)
                .setParameter("type", type)
                .setParameter("name", name)
                .setParameter("foundationYear", foundationYear)
                .setParameter("population", Program.MIN_POPULATION)
                .getResultList();
    }

    @Override
    public RawRegion getRegionByCapitalUrl(String url) {
        List<RawRegion> regionList = em.createQuery("from RawRegion where capitalUrl = :url", RawRegion.class)
                .setParameter("url", url)
                .getResultList();
        if (regionList.isEmpty()) {
            return null;
        }
        return regionList.get(0);
    }

    @Override
    public RawRegion getRegionByAdministrativeCenterUrl(String url) {
        List<RawRegion> regionList = em.createQuery("from RawRegion where administrativeCenterUrl = :url", RawRegion.class)
                .setParameter("url", url)
                .getResultList();
        if (regionList.isEmpty()) {
            return null;
        }
        return regionList.get(0);
    }

    @Override
    public List<NormalizedCity> getNormList(int skipCount, int takeCount) {
        return em.createQuery("from NormalizedCity where population >= :population order by name asc", NormalizedCity.class)
                .setFirstResult(skipCount)
                .setMaxResults(takeCount)
                .setParameter("population", Program.MIN_POPULATION)
                .getResultList();
    }

    @Override
    public List<String> getAllCityNames() {
        return em.createQuery("select name from NormalizedCity where population >= :population", String.class)
                .setParameter("population", Program.MIN_POPULATION)
                .getResultList();
    }

    @Override
    public Integer agregateNormByNameAndCountry(String function, String fieldName, String countryName, String name) {
        return em.createQuery("select " + function + "(n." + fieldName + ") from NormalizedCity n where n.country = :country and n.name != :name", Integer.class)
                .setParameter("country", countryName)
                .setParameter("name", name)
                .getSingleResult();
    }

    @Override
    public Integer agregateNormByNameAndRegion(String function, String fieldName, String countryName, String name, String regionName) {
        return em.createQuery("select " + function + "(n." + fieldName + ") from NormalizedCity n where n.country = :country and n.secondLocation = :regionName and n.name != :name", Integer.class)
                .setParameter("country", countryName)
                .setParameter("regionName", regionName)
                .setParameter("name", name)
                .getSingleResult();
    }

    @Override
    public RawCity getLatestRawCity() {
        List<RawCity> latestCities = em.createQuery("from RawCity where lastUpdate != null order by lastUpdate desc", RawCity.class)
                .setMaxResults(1)
                .getResultList();

        if (latestCities.isEmpty()) {
            return null;
        }
        return latestCities.get(0);
    }


    @Override
    @Transactional
    public void merge(Object entity) {
        em.merge(entity);
    }
}
