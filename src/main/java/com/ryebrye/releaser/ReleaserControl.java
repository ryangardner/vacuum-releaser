package com.ryebrye.releaser;

import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import org.apache.camel.Consume;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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

    @EndpointInject(uri = "seda:releaserControl")
    ProducerTemplate emptyReleaser;

    private enum ReleaserState {
        Filling, Full, Emptying, Empty
    }

    private enum ReleaserTrigger {
        SapAboveHighPoint, SapBelowHighPoint, SapAboveLowPoint, SapBelowLowPoint
    }

    StateMachine<ReleaserState, ReleaserTrigger> releaser;


    public ReleaserControl() {
        StateMachineConfig<ReleaserState, ReleaserTrigger> releaserStateConfig = new StateMachineConfig<>();

        releaserStateConfig.configure(ReleaserState.Filling)
                .permit(ReleaserTrigger.SapAboveHighPoint, ReleaserState.Full);

        releaserStateConfig.configure(ReleaserState.Full)
                .onEntry(this::handleFullReleaser)
                .permit(ReleaserTrigger.SapBelowHighPoint, ReleaserState.Emptying);

        releaserStateConfig.configure(ReleaserState.Emptying)
                .permit(ReleaserTrigger.SapBelowLowPoint, ReleaserState.Empty);

        releaserStateConfig.configure(ReleaserState.Empty)
                .onEntry(this::handleEmptyReleaser)
                .permit(ReleaserTrigger.SapAboveLowPoint, ReleaserState.Filling);

        releaser = new StateMachine<ReleaserState, ReleaserTrigger>(ReleaserState.Empty, releaserStateConfig);
    }

    private void handleFullReleaser() {
        log.info("sending open message");
        emptyReleaser.sendBody("open");
    }

    private void handleEmptyReleaser() {
        log.info("releaser is empty, sending message to start it filling again");
        emptyReleaser.sendBody("close");
    }

    @Consume(uri = "seda:highSwitchStateChange")
    public void handleHighSwitchStateChange(boolean highSwitchState) {
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
        if (lowSwitchState) {
            if (releaser.canFire(ReleaserTrigger.SapAboveLowPoint)) {
                releaser.fire(ReleaserTrigger.SapAboveLowPoint);
            } else {
                log.error("sap above the high point, but that's not valid for the state of {}", releaser.getState().name());
            }
        } else {
            if (releaser.canFire(ReleaserTrigger.SapBelowLowPoint)) {
                releaser.fire(ReleaserTrigger.SapBelowLowPoint);
            } else {
                log.error("Sap below high point is not a valid trigger for the state of {}", releaser.getState().name());
            }
        }
    }


}
