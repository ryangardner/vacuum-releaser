package com.ryebrye.releaser.weathersensors;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * @author Ryan Gardner
 * @date 2/5/15
 */
@Profile("!raspberryPi")
@Component
public class MockSensors implements TemperatureSensor, BarometricPressureSensor {
    @Override
    public double readPressureAsInHg() {
        return 29.68;
    }

    @Override
    public double readTemperature() {
        return 21.8;
    }
}
