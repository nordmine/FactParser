package ru.nordmine.text.generator.handler;

import com.google.common.base.Joiner;
import ru.nordmine.Program;
import ru.nordmine.entities.norm.NormalizedCity;
import ru.nordmine.text.generator.SentencePool;

import java.util.*;

public class CoordinateHandler extends ComparisonableCaption {

    private static SentencePool sentences = new SentencePool(new String[]{
            "составляет",
            "равняется",
            "приблизительно равно",
            "около",
            "примерно равно"
    });

    @Override
    public String[] getPossibleCaptions() {
        return new String[]{"координаты"};
    }

    @Override
    protected Map<String, List<String>> getSentences(String caption, String value, NormalizedCity normalizedCity) {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        List<String> lines = new LinkedList<String>();
        if (normalizedCity.getLatitude() != null && normalizedCity.getLatitude() != null) {
            String countryName = normalizedCity.getCountry();
            String type = normalizedCity.getType();
            List<NormalizedCity> infoboxList = storageService.getNormListByCountryAndType(countryName, type);

            Double curLat = Double.parseDouble(normalizedCity.getLatitude());
            Double curLong = Double.parseDouble(normalizedCity.getLongitude());

            Map<Long, String> longs = new TreeMap<Long, String>();

            for (NormalizedCity infobox : infoboxList) {
                if (infobox.getLatitude() != null && infobox.getLongitude() != null) {
                    Double lat = Double.parseDouble(infobox.getLatitude());
                    Double lng = Double.parseDouble(infobox.getLongitude());
                    long longKm = Math.round(distFrom(lat, lng, curLat.doubleValue(), curLong.doubleValue()) / 1000);
                    if (longKm > 1) {
                        longs.put(longKm, infobox.getName());
                    }
                }
            }

            if (longs.size() > 3) {
                String typesName = morpherService.loadFormsFromStorage(normalizedCity.getType()).getMultipleImen();
                String line = "Рядом расположены такие " + typesName + " как ";
                List<String> nearCities = new LinkedList<String>();
                for (Map.Entry<Long, String> entry : longs.entrySet()) {
                    if (nearCities.size() < 3) {
                        nearCities.add("{city|" + entry.getValue() + "} (" + entry.getKey() + " км)");
                    }
                }
                line += Joiner.on(", ").join(nearCities);
                lines.add(line);
            }

            map.put("geo", lines);

            List<String> statList = new LinkedList<String>();
            double moscowLat = 55.751666676667;
            double moscowLong = 37.617777787778;
            double longInMeters = distFrom(moscowLat, moscowLong, curLat.doubleValue(), curLong.doubleValue());
            statList.add("Расстояние до Москвы " + sentences.next() + " "
                    + Math.round(longInMeters / 1000) + " км");
            map.put("stat", statList);
        }
        return map;
    }

    private double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 3958.75;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = earthRadius * c;

        int meterConversion = 1609;

        return (dist * meterConversion);
    }
}
