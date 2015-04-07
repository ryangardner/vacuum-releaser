package com.ryebrye.releaser;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import org.apache.camel.Consume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * This class only runs when the "raspberryPi" profile is enabled - otherwise the application wont start because the
 * pi4j library will try to load the native library on your development machine.
 *
 * @author Ryan Gardner
 * @date 1/1/15
 */
@Profile("raspberryPi")
@Component
public class HardwareImpl {

    private static final Logger log = LoggerFactory.getLogger(HardwareImpl.class);
    public static final int DEBOUNCE_MS = 150;

    @Autowired
    protected ReleaserManagement releaserManagement;

    private final GpioController gpio = GpioFactory.getInstance();
    protected GpioPinDigitalInput lowFloatSwitch;
    protected GpioPinDigitalInput highFloatSwitch;
    protected GpioPinDigitalOutput releaserActiveLED;
    protected GpioPinDigitalOutput vacuumRelay;


    @PostConstruct
    public void initializeHardware() {
        log.info("Initializing hardware interface");
        log.info(" Pin 00 -> low level float switch. Should be high when it is active");

        lowFloatSwitch = gpio.provisionDigitalInputPin(RaspiPin.GPIO_00, PinPullResistance.PULL_DOWN);
        lowFloatSwitch.setDebounce(DEBOUNCE_MS);
        log.info(" Pin 01 -> high level float switch. Should be high when it is active");

        highFloatSwitch = gpio.provisionDigitalInputPin(RaspiPin.GPIO_01, PinPullResistance.PULL_DOWN);
        highFloatSwitch.setDebounce(DEBOUNCE_MS);
        log.info(" Pin 02 -> releaser activity LED");

        releaserActiveLED = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, PinState.LOW);

        log.info(" Pin 03 -> relay to solenoid valve (when in low state, this should have vacuum go to chamber. when high, atmospheric pressure should go to chamber)");
        vacuumRelay = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03, PinState.HIGH);

        // when the low or high switches change state, have the event listeners delegate to the broadcaster
        // to have it send the message out
        lowFloatSwitch.addListener((GpioPinListenerDigital) (GpioPinDigitalStateChangeEvent event) -> {
            log.debug("low float switch signal is now {}", event.getState().isHigh());
            releaserManagement.broadcastLowSwitchStatus(event.getState().isHigh());
        });
        highFloatSwitch.addListener((GpioPinListenerDigital) (GpioPinDigitalStateChangeEvent event) -> {
            log.debug("high float switch signal is now {}", event.getState().isHigh());
            releaserManagement.broadcastHighSwitchStatus(event.getState().isHigh());
        });

        // schedule setting the in setting the state based on the current value of the pins
        // do this with some delay bewteen them to avoid race conditions
        GpioFactory.getExecutorServiceFactory().getScheduledExecutorService().schedule(() -> {
            releaserManagement.broadcastLowSwitchStatus(lowFloatSwitch.getState().isHigh());
        }, 10, TimeUnit.MILLISECONDS);
        GpioFactory.getExecutorServiceFactory().getScheduledExecutorService().schedule(() -> {
            releaserManagement.broadcastLowSwitchStatus(highFloatSwitch.getState().isHigh());
        }, 200, TimeUnit.MILLISECONDS);
        // send the initial broadcast out about the current state
    }

    @Consume(uri = "seda:releaserHardwareControl")
    public void handleReleaserControl(Object message) {
        log.info("handling releaser control message '{}'", message);
        if ("close".equals(String.valueOf(message))) {
            log.info("Setting vacuum relay state to HIGH (closed) in response to control message");
            vacuumRelay.high();
            releaserActiveLED.low();
        } else {
            log.info("Setting vacuum relay state to LOW (open) in response to control message");
            vacuumRelay.low();
        }
    }


    @PreDestroy
    public void shutdownGpios() {
        log.info("Shutting down GPIO interface");
        releaserActiveLED.low();
        vacuumRelay.low();
        gpio.shutdown();
        log.info("GPIO interface shutdown");
    }


}
