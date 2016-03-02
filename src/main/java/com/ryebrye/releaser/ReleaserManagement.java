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

    @EndpointInject(uri = "seda:moistureTrapSwitchChange")
    protected ProducerTemplate moistureTrapSwitchChange;

    @EndpointInject(uri = "seda:releaserControl")
    ProducerTemplate emptyReleaser;

    @EndpointInject(uri = "seda:releaserVacuumPumpControl")
    ProducerTemplate controlPump;

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

    @ManagedOperation(description = "set the high switch status")
    public void broadcastMoistureTrapSwitchChange(boolean status) {
        moistureTrapSwitchChange.sendBody(status);
    }

    @ManagedOperation(description = "set the state of power to the vacuum pump")
    public void broadcastPumpState(boolean state) {
        controlPump.sendBody(state ? "on" : "off");
    }




}
