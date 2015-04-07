package com.ryebrye.releaser.weathersensors;

import com.github.dvdme.ForecastIOLib.FIOCurrently;
import com.github.dvdme.ForecastIOLib.FIODataPoint;
import com.github.dvdme.ForecastIOLib.ForecastIO;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @author Ryan Gardner
 * @date 2/5/15
 */
@Profile("!weatherSensorEquipped")
@Component
public class MockSensors implements TemperatureSensor, BarometricPressureSensor {

    private final String CACHE_KEY = "forecastio_data";
    // this cache is intended mainly to act as a simple way to throttle access to the Forecastio API
    private LoadingCache<String, FIODataPoint> data = CacheBuilder.newBuilder().maximumSize(3)
            .expireAfterWrite(1, TimeUnit.MINUTES).build(
                    new CacheLoader<String, FIODataPoint>() {
                        public FIODataPoint load(String string) {
                            return new FIOCurrently(forecastIO).get();
                        }
                    });

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
        try {
            return data.getUnchecked(CACHE_KEY).pressure() * 0.0295301;
        }
        catch(Exception e) {
            return 29.4;
        }
    }

    @Override
    public double readTemperature() {
        return (readTemperatureFarenheit() - 32) * 5 / 9;
    }

    @Override
    public double readTemperatureFarenheit() {
        try {
            return data.getUnchecked(CACHE_KEY).temperature();
        } catch (Exception e) {
            return 33.0;
        }
    }
}
