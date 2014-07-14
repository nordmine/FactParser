package ru.nordmine;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.nordmine.services.CrawlerService;
import ru.nordmine.services.NormalizationService;
import ru.nordmine.services.TextGeneratorService;
import ru.nordmine.services.UpdaterService;

public class Program {

    public static final int MIN_POPULATION = 110000;
    public static final int TAKE_COUNT = 100;
    private ApplicationContext context;

    public static String siteBaseAddress = "http://tourto.test/";

    public static void main(String[] args) {
        Program loader = new Program();
        String countryName = "Россия";
        if (args.length > 0) {
            countryName = args[0];
        }
        if (args.length > 1 && args[1].equalsIgnoreCase("production")) {
            siteBaseAddress = "http://tourto.ru/";
        }
        loader.run(countryName);
    }

    public void run(String commandName) {
        context = new ClassPathXmlApplicationContext("META-INF/beans.xml");

        if (commandName.equals("crawler")) {
            CrawlerService infoboxService = (CrawlerService) context.getBean("crawlerService");
            int counter = 0;

            String nextPage = "";
            while (counter < 1000 && nextPage != null) {
                nextPage = infoboxService.crawlCitiesFromPage(nextPage);
                counter++;
            }
        }

        if (commandName.equals("normalize")) {
            batchNormalization();
        }

        if (commandName.equals("update")) {
            batchUpdating();
        }

        TextGeneratorService generator = (TextGeneratorService) context.getBean("generatorService");
        generator.createArticleForRegion(commandName);
    }

    private void batchUpdating() {
        UpdaterService updaterService = (UpdaterService) context.getBean("updaterService");
        int updateCounter, stepCounter = 0;
        do {
            updateCounter = updaterService.updateCities(stepCounter * TAKE_COUNT, TAKE_COUNT);
            stepCounter++;
        } while (updateCounter > 0);

        stepCounter = 0;
        do {
            updateCounter = updaterService.updateRegions(stepCounter * TAKE_COUNT, TAKE_COUNT);
            stepCounter++;
        } while (updateCounter > 0);
    }

    private void batchNormalization() {
        NormalizationService infoboxService = (NormalizationService) context.getBean("normalizationService");

        int affectedRows, stepCounter = 0;
        do {
            affectedRows = infoboxService.normalizeCities(stepCounter * TAKE_COUNT, TAKE_COUNT);
            stepCounter++;
        } while (affectedRows > 0);

        stepCounter = 0;
        do {
            affectedRows = infoboxService.normalizeRegions(stepCounter * TAKE_COUNT, TAKE_COUNT);
            stepCounter++;
        } while (affectedRows > 0);
    }

}
