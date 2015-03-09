package com.ryebrye.releaser

import com.github.oxo42.stateless4j.StateMachine
import com.ryebrye.releaser.weathersensors.TemperatureSensor
import org.apache.camel.ProducerTemplate
import spock.lang.Specification

/**
 * @author Ryan Gardner
 * @date 12/31/14
 */
class ReleaserControlSpec extends Specification {

    def "When the releaser is filling and the high switch is tripped, a message is sent to dump the releaser"() {
        setup:
            ReleaserControl releaserControl = new ReleaserControl();
            releaserControl.releaserSettingsRepository = Mock(ReleaserSettingsRepository) {
                findReleaserSettings() >>
                        Mock(ReleaserSettings) { getGallonsPerDump() >> { return 1.2 } }
            }
            releaserControl.temperatureSensor = Mock(TemperatureSensor) { readTemperature() >> { return 34 } }
            releaserControl.releaser = new StateMachine<ReleaserControl.ReleaserState, ReleaserControl.ReleaserTrigger>(ReleaserControl.ReleaserState.Filling, releaserControl.initialConfiguration());
            def mock = Mock(ProducerTemplate)
            releaserControl.emptyReleaser = mock
        when:
            releaserControl.handleHighSwitchStateChange(true)
        then:
            1 * mock.sendBody(_)
    }

    def "when the high switch is tripped but the releaser is already releasing, no control message is sent"() {
        setup:
            ReleaserControl releaserControl = new ReleaserControl();
            releaserControl.releaser = new StateMachine<ReleaserControl.ReleaserState, ReleaserControl.ReleaserTrigger>(ReleaserControl.ReleaserState.Emptying, releaserControl.initialConfiguration());
            def mock = Mock(ProducerTemplate)
            releaserControl.emptyReleaser = mock
        when:
            releaserControl.handleHighSwitchStateChange(true)
        then:
            0 * mock.sendBody(_)
    }

    def "when emptying, when the low switch is no longer active, a control message is sent"() {
        setup:
            ReleaserControl releaserControl = new ReleaserControl();
            releaserControl.releaser = new StateMachine<ReleaserControl.ReleaserState, ReleaserControl.ReleaserTrigger>(ReleaserControl.ReleaserState.Emptying, releaserControl.initialConfiguration());

            def mock = Mock(ProducerTemplate)
            releaserControl.emptyReleaser = mock
        when:
            releaserControl.handleLowSwitchStateChange(false)
        then:
            1 * mock.sendBody("close")
    }

    def "when the low switch is sent high and the releaser is not open, no message is sent"() {
        setup:
            ReleaserControl releaserControl = new ReleaserControl();
//            releaserControl.highSwitchActive = false
//            releaserControl.lowSwitchActive = true
//            releaserControl.releasingInProgress = false
            def mock = Mock(ProducerTemplate)
            releaserControl.emptyReleaser = mock
        when:
            releaserControl.handleLowSwitchStateChange(false)
        then:
            0 * mock.sendBody(_)

    }

}
