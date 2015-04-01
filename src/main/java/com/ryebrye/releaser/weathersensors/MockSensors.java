package com.ryebrye.releaser.weathersensors;

import com.github.dvdme.ForecastIOLib.FIOCurrently;
import com.github.dvdme.ForecastIOLib.ForecastIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Ryan Gardner
 * @date 2/5/15
 */
@Profile("!weatherSensorEquipped")
@Component
public class MockSensors implements TemperatureSensor, BarometricPressureSensor {

    @Value("${forecastio.apikey}")
    private String apikey;

    @Value("${forecastio.lat}")
    private String lat;

    @Value("${forecastio.longitude}")
    private String longitude;

    ForecastIO forecastIO;

    @PostConstruct
    private void initializeWeatherLib() {
        forecastIO = new ForecastIO(lat, longitude, ForecastIO.UNITS_US, "en", apikey);
    }

    ThreadLocalRandom tlr = ThreadLocalRandom.current();

    @Override
    public double readPressureAsInHg() {
        return 29.68;
    }

    public double readTemperature() {
        FIOCurrently currently = new FIOCurrently(forecastIO);
        return currently.get().temperature();
    }

    @Override
    public double readTemperatureFarenheit() {
        FIOCurrently currently = new FIOCurrently(forecastIO);
        return currently.get().temperature();
    }
}
