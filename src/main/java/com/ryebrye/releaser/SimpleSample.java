package com.ryebrye.releaser;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.gpio.trigger.GpioCallbackTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;

/**
 * Created by ryangardner on 12/29/14.
 */
//@Component
public class SimpleSample implements CommandLineRunner {
    public static final Logger log = LoggerFactory.getLogger(SimpleSample.class);

    @Override
    public void run(String... strings) throws Exception {
        long sleepConstant = strings.length > 0 ? Long.parseLong(strings[0]) : 1000;
        log.info("starting to run sample");
        final GpioController gpio = GpioFactory.getInstance();
        final GpioPinDigitalOutput redLight = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, PinState.LOW);
        final GpioPinDigitalOutput yellowLight = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, PinState.LOW);
        final GpioPinDigitalOutput greenLight = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03, PinState.LOW);
        final GpioPinDigitalInput pushButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_00, PinPullResistance.PULL_DOWN);

        GpioPinDigitalOutput[] lights = {redLight, yellowLight, greenLight};

        pushButton.addTrigger(new GpioCallbackTrigger(() -> {
            log.info("GPIO TRIGGER CALLBACK RECEIVED ");
            return null;
        }));

        pushButton.addListener((GpioPinListenerDigital) (GpioPinDigitalStateChangeEvent event) -> {
            log.info("Somone pushed my BUTTON and state is now {}", event.getState().getName());
        });

        log.info("setting output high");
        for (GpioPinDigitalOutput output : lights) {
            output.high();
        }
        Thread.sleep(sleepConstant);
        log.info("setting low");
        for (GpioPinDigitalOutput output : lights) {
            output.low();
        }

        while(true) {
            Thread.sleep(500);
        }
    }
}
