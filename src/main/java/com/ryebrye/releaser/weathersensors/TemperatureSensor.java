package com.ryebrye.releaser.weathersensors;

/**
 * @author Ryan Gardner
 * @date 2/5/15
 */
public interface TemperatureSensor {
    /**
     * return the temperature as celcius
     * @return
     */
    double readTemperature();

    public default double readTemperatureFarenheit() {
        return celciusToFarenheit(readTemperature());
    }

    public default double celciusToFarenheit(double celcius) {
        return ((40 + celcius) * 1.8) - 40;
    }
}
