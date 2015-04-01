package com.ryebrye.releaser.doublereleaser;

import com.ryebrye.releaser.ReleaserSettingsRepository;
import com.ryebrye.releaser.historical.ReleaserEvent;
import com.ryebrye.releaser.weathersensors.TemperatureSensor;
import com.ryebrye.releaser.weathersensors.WeatherSensorController;
import org.apache.camel.Consume;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

/**
 * @author Ryan Gardner
 * @date 3/31/15
 */
@Component
@ManagedResource(objectName = "ryebrye:name=doubleReleaser", description = "set statuses without having access to gpio pins (for local development etc)")
public class DoubleReleaserControl {
    private static final Logger log = LoggerFactory.getLogger(DoubleReleaserControl.class);


    public enum ReleaserSide {
        LEFT("left"),
        RIGHT("right");

        ReleaserSide(String side) {
            this.side = side;
        }

        private final String side;

        public String getSide() {
            return side;
        }
    }

    @Autowired
    TemperatureSensor temperatureSensor;

    @Autowired
    ReleaserSettingsRepository releaserSettingsRepository;

    @EndpointInject(uri = "seda:processCompletedEvent")
    ProducerTemplate releaserDumpingTemplate;

    @Consume(uri = "direct:doubleReleaserDump")
    public void handleDoubleReleaserDumping(ReleaserSide releaserSide) {
        log.info("{} side is dumping now", releaserSide.getSide());
        ReleaserEvent releaserEvent = new ReleaserEvent();
        releaserEvent.setStartTime(ZonedDateTime.now());
        releaserEvent.setEndTime(ZonedDateTime.now());
        releaserEvent.setTemperature(temperatureSensor.readTemperatureFarenheit());
        releaserEvent.setSapQuantity(releaserSettingsRepository.findReleaserSettings().getGallonsPerFullDump());
        releaserDumpingTemplate.sendBody(releaserEvent);
    }


    // seems a tad ridiculous to farm things off to camel instead of calling method directly, but this
    // makes this simulate the actual route from hardware more closely
    @EndpointInject(uri="direct:doubleReleaserDump")
    ProducerTemplate sendDumpMessage;

    @ManagedOperation(description = "dump sap from the left side")
    public void dumpLeftSide() {
        sendDumpMessage.sendBody(ReleaserSide.LEFT);
    }

    @ManagedOperation(description = "dump sap from the right side")
    public void dumpRightSide() {
        sendDumpMessage.sendBody(ReleaserSide.RIGHT);
    }


}
