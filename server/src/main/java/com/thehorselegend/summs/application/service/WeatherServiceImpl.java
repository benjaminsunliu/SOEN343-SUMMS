package com.thehorselegend.summs.application.service;

import com.thehorselegend.summs.api.weather.Severity;
import com.thehorselegend.summs.api.weather.WeatherCondition;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class WeatherServiceImpl implements WeatherService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public WeatherCondition getCurrentWeather(double lat, double lon) {
        String url = String.format(
                "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&current_weather=true",
                lat, lon
        );

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        Map<String, Object> currentWeather = (Map<String, Object>) response.get("current_weather");

        int weatherCode = (Integer) currentWeather.get("weathercode");
        String weatherType = mapWeatherCodeToType(weatherCode);
        Severity severity = mapWeatherTypeToSeverity(weatherType);

        return new WeatherCondition(weatherType, severity);
    }

    private String mapWeatherCodeToType(int code) {
        if (code >= 51 && code <= 67) return "Rain";
        if (code >= 71 && code <= 77) return "Snow";
        if (code == 0) return "Clear";
        if (code == 1) return "PartlyCloudy";
        return "Clouds";
    }

    private Severity mapWeatherTypeToSeverity(String type) {
        switch (type) {
            case "Rain": return Severity.HIGH;
            case "Snow": return Severity.HIGH;
            case "Clouds": return Severity.MEDIUM;
            default: return Severity.LOW;
        }
    }
}