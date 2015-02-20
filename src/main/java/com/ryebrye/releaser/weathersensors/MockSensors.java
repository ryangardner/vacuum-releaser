package com.ryebrye.releaser.weathersensors;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Ryan Gardner
 * @date 2/5/15
 */
@Profile("!raspberryPi")
@Component
public class MockSensors implements TemperatureSensor, BarometricPressureSensor {
    ThreadLocalRandom tlr = ThreadLocalRandom.current();

    @Override
    public double readPressureAsInHg() {
        return 29.68;
    }

    @Override
    public double readTemperature() {
        return 21.8 + tlr.nextDouble(-2,3.5);
    }
}
