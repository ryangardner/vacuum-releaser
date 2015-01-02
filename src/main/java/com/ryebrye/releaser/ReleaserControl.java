package com.ryebrye.releaser;

import org.apache.camel.Consume;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * This class holds the state of the releaser.
 * <p/>
 * @author Ryan Gardner
 * @date 12/31/14
 */
@Component
public class ReleaserControl {
    private static final Logger log = LoggerFactory.getLogger(ReleaserControl.class);

    // store the state that we are aware of here.
    private boolean highSwitchActive = false;
    private boolean lowSwitchActive = false;

    // this is when we should be in the "drain" state
    private boolean releasingInProgress = false;

    @EndpointInject(uri = "seda:releaserControl")
    ProducerTemplate emptyReleaser;

    @Consume(uri="seda:highSwitchStateChange")
    public void handleHighSwitchStateChange(boolean state) {
        if (this.highSwitchActive != state) {
            this.highSwitchActive = state;
        }
        if (this.highSwitchActive) {
            if (!this.releasingInProgress) {
                log.info("sending open message");
                this.releasingInProgress = true;
                emptyReleaser.sendBody("open");
            } else {
                log.error("the switch went high again while we were already releasing");
            }
        }
        if (!this.highSwitchActive && this.releasingInProgress) {
            log.info("High switch is not active anymore - draining is working");
        }
    }

    @Consume(uri="seda:lowSwitchStateChange")
    public void handleLowSwitchStateChange(boolean state) {
        if (this.lowSwitchActive != state) {
            this.lowSwitchActive = state;
        }
        if (this.releasingInProgress && !this.lowSwitchActive) {
            this.releasingInProgress = false;
            if (this.highSwitchActive) {
                log.error("The high switch is still active - the switch may be malfunctioning.");
            }
            log.info("low switch no longer active - releasing process is complete");
            // send message to close the releaser
            emptyReleaser.sendBody("close");
        }
        else {
            if (this.lowSwitchActive && !this.releasingInProgress) {
               log.info("the low switch is active again - this indicates is filling properly.");
            }
        }
    }


}
