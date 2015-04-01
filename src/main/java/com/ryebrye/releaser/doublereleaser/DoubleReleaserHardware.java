package com.ryebrye.releaser.doublereleaser;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.ryebrye.releaser.ReleaserSettingsRepository;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;

/**
 * @author Ryan Gardner
 * @date 3/31/15
 */
@Profile("raspberryPi")
public class DoubleReleaserHardware {
    private static final Logger log = LoggerFactory.getLogger(DoubleReleaserHardware.class);

    @Autowired
    private ReleaserSettingsRepository releaserSettingsRepository;

    @EndpointInject(uri = "direct:doubleReleaserDump")
    private ProducerTemplate hardwareMessage;

    private long debounceMS = 250;

    private final GpioController gpio = GpioFactory.getInstance();
    protected GpioPinDigitalInput leftSideDumpingSwitch;
    protected GpioPinDigitalInput rightSideDumpingSwitch;

    @PostConstruct
    public void initializeHardware() {
        log.info("Initializing hardware interface");
        log.info(" Pin 10 -> low side switch. Should complete circuit to ground when it is active");
        log.info(" Pin 11 -> right side switch. Should complete circuit to ground when it is active");

        leftSideDumpingSwitch = gpio.provisionDigitalInputPin(RaspiPin.GPIO_10, PinPullResistance.PULL_DOWN);
        rightSideDumpingSwitch = gpio.provisionDigitalInputPin(RaspiPin.GPIO_11, PinPullResistance.PULL_DOWN);

        leftSideDumpingSwitch.addListener((GpioPinListenerDigital) (GpioPinDigitalStateChangeEvent event) -> {
            log.debug("left float switch signal is now {}", event.getState().isHigh());
            if (event.getState().isHigh()) {
                hardwareMessage.sendBody(DoubleReleaserControl.ReleaserSide.LEFT);
            }
        });
        rightSideDumpingSwitch.addListener((GpioPinListenerDigital) (GpioPinDigitalStateChangeEvent event) -> {
            log.debug("right float switch signal is now {}", event.getState().isHigh());
            if (event.getState().isHigh()) {
                hardwareMessage.sendBody(DoubleReleaserControl.ReleaserSide.RIGHT);
            }
        });

    }

    private static class doubleReleaserListener implements GpioPinListenerDigital {
        private static final Logger log = LoggerFactory.getLogger(doubleReleaserListener.class);

        @Override
        public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {

        }
    }


}
