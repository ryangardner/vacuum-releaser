package com.ryebrye.releaser.weathersensors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ryan Gardner
 * @date 2/5/15
 */
@RestController
public class WeatherSensorController {
    @Autowired
    private TemperatureSensor temperatureSensor;
    @Autowired
    private BarometricPressureSensor barometricPressureSensor;

    @RequestMapping("/sensors/weather")
    public Map<String,Double> weatherSensors() {
        HashMap<String,Double> sensorData = new HashMap<>(4);
        sensorData.put("temperature", temperatureSensor.readTemperature());
        sensorData.put("pressure", barometricPressureSensor.readPressureAsInHg());
        return sensorData;
    }
}
