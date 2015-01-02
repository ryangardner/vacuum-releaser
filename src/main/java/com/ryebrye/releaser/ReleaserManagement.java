package com.ryebrye.releaser;

import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

/**
 * @author Ryan Gardner
 * @date 12/31/14
 */
@Component
@ManagedResource(objectName = "ryebrye:name=mockHardware", description = "set statuses without having access to gpio pins (for local development etc)")
public class ReleaserManagement {

    @EndpointInject(uri = "seda:highSwitchStateChange")
    protected ProducerTemplate highSwitchStateChange;

    @EndpointInject(uri = "seda:lowSwitchStateChange")
    protected ProducerTemplate lowSwitchStateChange;

    @EndpointInject(uri = "seda:releaserControl")
    ProducerTemplate emptyReleaser;

    @ManagedOperation(description = "set the high switch status")
    public void broadcastHighSwitchStatus(boolean status) {
        highSwitchStateChange.sendBody(status);
    }

    @ManagedOperation(description = "set the high switch status")
    public void broadcastLowSwitchStatus(boolean status) {
        lowSwitchStateChange.sendBody(status);
    }

    @ManagedOperation(description = "send the open / close message for the releaser relay")
    public void setReleaserOpenState(boolean state) {
       emptyReleaser.sendBody(state ? "open" : "close");
    }



}
