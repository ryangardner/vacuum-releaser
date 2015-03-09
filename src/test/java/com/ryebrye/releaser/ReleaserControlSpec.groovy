package com.ryebrye.releaser

import com.github.oxo42.stateless4j.StateMachine
import com.ryebrye.releaser.weathersensors.TemperatureSensor
import org.apache.camel.ProducerTemplate
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Ryan Gardner
 * @date 12/31/14
 */
class ReleaserControlSpec extends Specification {

    @Shared
    ReleaserControl releaserControl


    def setup() {
        releaserControl = new ReleaserControl();

        releaserControl.releaserSettingsRepository = Mock(ReleaserSettingsRepository) {
            findReleaserSettings() >>
                    Mock(ReleaserSettings) { getGallonsPerDump() >> { return 1.2 } }
        }
        releaserControl.temperatureSensor = Mock(TemperatureSensor) { readTemperature() >> { return 34 } }
    }

    def "When the releaser is filling and the high switch is tripped, a message is sent to dump the releaser"() {
        setup:
            releaserControl.releaser = new StateMachine<ReleaserControl.ReleaserState, ReleaserControl.ReleaserTrigger>
                    (ReleaserControl.ReleaserState.Filling, releaserControl.initialConfiguration());
            ProducerTemplate mockProducerTemplate = Mock(ProducerTemplate)
            releaserControl.emptyReleaser = mockProducerTemplate
        when:
            releaserControl.handleHighSwitchStateChange(true)
        then:
            1 * mockProducerTemplate.sendBody(_)
    }

    def "when the high switch is tripped but the releaser is already releasing, no control message is sent"() {
        setup:
            releaserControl.releaser = new StateMachine<ReleaserControl.ReleaserState, ReleaserControl.ReleaserTrigger>(ReleaserControl.ReleaserState.Emptying, releaserControl.initialConfiguration());
            ProducerTemplate mockProducerTemplate = Mock(ProducerTemplate)
            releaserControl.emptyReleaser = mockProducerTemplate
        when:
            releaserControl.handleHighSwitchStateChange(true)

        then:
            0 * mockProducerTemplate.sendBody(_)
    }

    def "when emptying, when the low switch is no longer active, a control message is sent"() {
        setup:
            releaserControl.releaser = new StateMachine<ReleaserControl.ReleaserState, ReleaserControl.ReleaserTrigger>(ReleaserControl.ReleaserState.Emptying, releaserControl.initialConfiguration());
            ProducerTemplate mockProducerTemplate = Mock(ProducerTemplate)
            releaserControl.emptyReleaser = mockProducerTemplate
        when:
            releaserControl.handleLowSwitchStateChange(false)
        then:
            1 * mockProducerTemplate.sendBody("close")
    }

    def "when the low switch is sent low and the releaser is not open, no message is sent"() {
        setup:
            releaserControl.releaser = new StateMachine<ReleaserControl.ReleaserState, ReleaserControl.ReleaserTrigger>(ReleaserControl.ReleaserState.Empty, releaserControl.initialConfiguration());
            ProducerTemplate mockProducerTemplate = Mock(ProducerTemplate)
            releaserControl.emptyReleaser = mockProducerTemplate
        when:
            releaserControl.handleLowSwitchStateChange(false)
        then:
            0 * mockProducerTemplate.sendBody(_)
    }

    def "after going into the 'emptying' state, the sap low switch is ignored and no message is sent during the cooldown period"() {
        setup:
            releaserControl.releaser = new StateMachine<ReleaserControl.ReleaserState, ReleaserControl.ReleaserTrigger>(ReleaserControl.ReleaserState.Filling, releaserControl.initialConfiguration());
            releaserControl.COOLDOWN_PERIOD = 200;
            ProducerTemplate mockProducerTemplate = Mock(ProducerTemplate)
            releaserControl.emptyReleaser = mockProducerTemplate
        when:
            // be full
            releaserControl.handleHighSwitchStateChange(true)
            releaserControl.handleHighSwitchStateChange(false)
            // mark it as emptied
            releaserControl.handleLowSwitchStateChange(false)
        then:
            1 * mockProducerTemplate.sendBody(_)
    }

    def "after going into the 'emptying' state, after the cooldown if the switch is in the low state a close message will be sent"() {
        setup:
            releaserControl.releaser = new StateMachine<ReleaserControl.ReleaserState, ReleaserControl.ReleaserTrigger>(ReleaserControl.ReleaserState.Filling, releaserControl.initialConfiguration());
            releaserControl.COOLDOWN_PERIOD = 200;
            ProducerTemplate mockProducerTemplate = Mock(ProducerTemplate)
            releaserControl.emptyReleaser = mockProducerTemplate
        when:
            // be full
            releaserControl.handleHighSwitchStateChange(true)
            releaserControl.handleHighSwitchStateChange(false)
            // mark it as emptied
            releaserControl.handleLowSwitchStateChange(false)
            releaserControl.handleLowSwitchStateChange(true)
            releaserControl.handleLowSwitchStateChange(false)
            releaserControl.handleLowSwitchStateChange(true)
            releaserControl.handleLowSwitchStateChange(false)
        then:
            1 * mockProducerTemplate.sendBody(_)
        when:
            Thread.sleep(700)
        then:
            1 * mockProducerTemplate.sendBody("close")
    }


    def "after the cooldown period has ended, a 'empty' message will cause it to dump"() {
        setup:
            releaserControl.releaser = new StateMachine<ReleaserControl.ReleaserState, ReleaserControl.ReleaserTrigger>(ReleaserControl.ReleaserState.Filling, releaserControl.initialConfiguration());
            releaserControl.COOLDOWN_PERIOD = 150;
            ProducerTemplate mockProducerTemplate = Mock(ProducerTemplate)
            releaserControl.emptyReleaser = mockProducerTemplate
        when:
            // be full
            releaserControl.handleHighSwitchStateChange(true)
            releaserControl.handleHighSwitchStateChange(false)
            // mark it as emptied
            Thread.sleep(175)
            releaserControl.handleLowSwitchStateChange(false)
        then:
            2 * mockProducerTemplate.sendBody(_)
    }

}
