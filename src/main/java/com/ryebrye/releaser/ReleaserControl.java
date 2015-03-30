package com.ryebrye.releaser;

import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.ryebrye.releaser.historical.ReleaserEvent;
import com.ryebrye.releaser.weathersensors.TemperatureSensor;
import org.apache.camel.Consume;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This class manages the state of the releaser and responds to hardware events appropriately.
 * <p/>
 *
 * @author Ryan Gardner
 * @date 12/31/14
 */
@Component
public class ReleaserControl {
    private static final Logger log = LoggerFactory.getLogger(ReleaserControl.class);

    @Autowired
    TemperatureSensor temperatureSensor;

    @Autowired
    ReleaserSettingsRepository releaserSettingsRepository;

    @EndpointInject(uri = "seda:releaserControl")
    ProducerTemplate emptyReleaser;

    private enum ReleaserState {
        Filling, Full, Emptying, Empty
    }

    private ScheduledThreadPoolExecutor cooldownExecutor = new ScheduledThreadPoolExecutor(1, new ThreadFactoryBuilder().setNameFormat("releaser-control-%d").build());

    private enum ReleaserTrigger {
        SapAboveHighPoint, SapBelowHighPoint, SapAboveLowPoint, SapBelowLowPoint
    }

    private long lastReleaseTimeMillis = 0;
    protected static long COOLDOWN_PERIOD = TimeUnit.SECONDS.toMillis(2);

    private boolean sapAboveLowPoint = false;
    private boolean sapAboveHighPoint = false;

    StateMachine<ReleaserState, ReleaserTrigger> releaser;


    public ReleaserControl() {
        StateMachineConfig<ReleaserState, ReleaserTrigger> releaserStateConfig = initialConfiguration();

        releaser = new StateMachine<ReleaserState, ReleaserTrigger>(ReleaserState.Empty, initialReleaserConfig(releaserStateConfig));
    }

    // split out to make it easier to test
    protected StateMachineConfig<ReleaserState, ReleaserTrigger> initialConfiguration() {
        StateMachineConfig<ReleaserState, ReleaserTrigger> releaserStateConfig = new StateMachineConfig<>();

        releaserStateConfig.configure(ReleaserState.Filling)
                .permit(ReleaserTrigger.SapAboveHighPoint, ReleaserState.Full);

        releaserStateConfig.configure(ReleaserState.Full)
                .onEntry(this::handleFullReleaser)
                .onExit(this::scheduleCooldownCheck)
                .permit(ReleaserTrigger.SapBelowHighPoint, ReleaserState.Emptying);


        releaserStateConfig.configure(ReleaserState.Emptying)
                .permitIf(ReleaserTrigger.SapBelowLowPoint, ReleaserState.Empty, this::isCooledDown);


        releaserStateConfig.configure(ReleaserState.Empty)
                .onEntry(this::handleEmptyReleaser)
                .permit(ReleaserTrigger.SapAboveLowPoint, ReleaserState.Filling);
        return releaserStateConfig;
    }

    private StateMachineConfig<ReleaserState, ReleaserTrigger> initialReleaserConfig(StateMachineConfig<ReleaserState, ReleaserTrigger> releaserStateConfig) {
        return releaserStateConfig;
    }

    private void scheduleCooldownCheck() {
        cooldownExecutor.schedule(() -> {
            if (!(this.sapAboveLowPoint)) {
                releaser.fire(ReleaserTrigger.SapBelowLowPoint);
            }
        }, COOLDOWN_PERIOD, TimeUnit.MILLISECONDS);
    }

    private boolean isCooledDown() {
        return (System.currentTimeMillis() - lastReleaseTimeMillis) > COOLDOWN_PERIOD;
    }

    private void handleFullReleaser() {
        log.info("sending open message");
        ReleaserEvent startEvent = new ReleaserEvent();
        lastReleaseTimeMillis = System.currentTimeMillis();
        startEvent.setStartTime(ZonedDateTime.now());
        startEvent.setSapQuantity(releaserSettingsRepository.findReleaserSettings().getGallonsPerFullDump());
        startEvent.setTemperature(temperatureSensor.readTemperatureFarenheit());
        emptyReleaser.sendBody(startEvent);
    }

    private void handleEmptyReleaser() {
        log.info("releaser is empty, sending message to start it filling again");
        emptyReleaser.sendBody("close");
    }

    @Consume(uri = "seda:highSwitchStateChange")
    public void handleHighSwitchStateChange(boolean highSwitchState) {
        sapAboveHighPoint = highSwitchState;
        if (highSwitchState) {
            if (releaser.canFire(ReleaserTrigger.SapAboveHighPoint)) {
                releaser.fire(ReleaserTrigger.SapAboveHighPoint);
            } else {
                log.error("sap above the high point, but that's not valid for the state of {}", releaser.getState().name());
            }
        } else {
            if (releaser.canFire(ReleaserTrigger.SapBelowHighPoint)) {
                releaser.fire(ReleaserTrigger.SapBelowHighPoint);
            } else {
                log.error("Sap below high point is not a valid trigger for the state of {}", releaser.getState().name());
            }
        }
    }

    @Consume(uri = "seda:lowSwitchStateChange")
    public void handleLowSwitchStateChange(boolean lowSwitchState) {
        sapAboveLowPoint = lowSwitchState;
        if (lowSwitchState) {
            if (releaser.canFire(ReleaserTrigger.SapAboveLowPoint)) {
                releaser.fire(ReleaserTrigger.SapAboveLowPoint);
            } else {
                log.error("sap above the low point, but that's not valid for the state of {}", releaser.getState().name());
            }
        } else {
            if (releaser.canFire(ReleaserTrigger.SapBelowLowPoint)) {
                releaser.fire(ReleaserTrigger.SapBelowLowPoint);
            } else {
                log.error("Sap below low point is not a valid trigger for the state of {}", releaser.getState().name());
            }
        }
    }


}
