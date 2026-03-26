package com.thehorselegend.summs.application.service;

import com.thehorselegend.summs.api.weather.WeatherCondition;

public interface WeatherService {
    WeatherCondition getCurrentWeather(double lat, double lon);
}
