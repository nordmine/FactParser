package ru.nordmine.services;

import com.google.common.base.Splitter;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.nordmine.Program;
import ru.nordmine.StringHelper;
import ru.nordmine.entities.norm.NormalizedCity;
import ru.nordmine.entities.raw.RawRegion;
import ru.nordmine.storage.StorageService;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service("updaterService")
public class UpdaterServiceImpl implements UpdaterService {

    private static Logger logger = Logger.getLogger(UpdaterServiceImpl.class);

    @Autowired
    private TextGeneratorService textGenerator;

    @Autowired
    private StorageService storageService;

    @Override
    public int updateCities(int skipCount, int takeCount) {
        Document request = DocumentHelper.createDocument();
        Element rootElement = request.addElement("request");
        List<NormalizedCity> normalizedCities = storageService.getNormList(skipCount, takeCount);

        for (NormalizedCity norm : normalizedCities) {
            if (norm.getPopulation() < Program.MIN_POPULATION) {
                throw new RuntimeException("what the fuck? population = " + norm.getPopulation());
            }
            Element el = rootElement.addElement("city");

            Element countryEl = el.addElement("country");
            countryEl.setText(norm.getCountry());

            if (norm.getSecondLocation() != null) {
                Element secondRegionEl = el.addElement("second_region");
                secondRegionEl.setText(norm.getSecondLocation());
            }

            Element typeEl = el.addElement("city_type");
            typeEl.setText(StringHelper.upperFirstChar(norm.getType()));
            Element nameEl = el.addElement("name");
            nameEl.setText(norm.getName());

            Map<String, String> articleParts = textGenerator.createArticleForCity(norm.getName());

            if(!articleParts.containsKey("text")) {
                rootElement.remove(el);
                continue;
            }
            Element textEl = el.addElement("text");
            textEl.setText(articleParts.get("text"));

            Element statEl = el.addElement("stat");
            statEl.setText(articleParts.get("stat"));

            if (norm.getPopulation() != null) {
                Element populationEl = el.addElement("population");
                populationEl.setText(norm.getPopulation().toString());
            }

            if (norm.getFoundationYear() != null) {
                Element foundationYearEl = el.addElement("foundation_year");
                foundationYearEl.setText(norm.getFoundationYear().toString());
            }
        }

        String xml = request.asXML();
        logger.info(xml);
        int updateCounter = executeRequest(xml, "add_cities");
        return updateCounter;
    }

    @Override
    public int updateRegions(int skipCount, int takeCount) {
        Document request = DocumentHelper.createDocument();
        Element rootElement = request.addElement("request");
        List<RawRegion> regions = storageService.getRawListWhereRawNotEmpty(RawRegion.class, skipCount, takeCount);
        for (RawRegion region : regions) {
            Element el = rootElement.addElement("region");

            Element nameEl = el.addElement("name");
            nameEl.setText(region.getText());

            Map<String, String> map = textGenerator.createArticleForRegion(region.getText());

            Element textEl = el.addElement("text");
            textEl.setText(map.get("text"));

            if (map.containsKey("polity")) {
                Element polityEl = el.addElement("polity");
                polityEl.setText(map.get("polity"));
            }
        }

        String xml = request.asXML();
        logger.info(xml);
        int updateCounter = executeRequest(xml, "update_regions");
        return updateCounter;
    }

    private int executeRequest(String xml, String action) {
        int updateCounter = 0;
        String url = Program.siteBaseAddress + "updater/" + action;
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);
        post.setEntity(new StringEntity(xml, "UTF-8"));

        HttpResponse response;
        try {
            response = client.execute(post);
            logger.info("Response Code: " + response.getStatusLine().getStatusCode());
            String responseString = EntityUtils.toString(response.getEntity());
            logger.info("Response: " + responseString);
            updateCounter = Splitter.on('\n').omitEmptyStrings().splitToList(responseString).size();
            logger.info(updateCounter + " entities updated on " + Program.siteBaseAddress);
        } catch (IOException e) {
            logger.error(e);
        }
        return updateCounter;
    }
}
