package com.ryebrye.releaser

import org.apache.camel.ProducerTemplate
import spock.lang.Specification

/**
 * @author Ryan Gardner
 * @date 12/31/14
 */
class ReleaserControlSpec extends Specification {

    def "When the high switch is tripped, a message is sent to dump the releaser"() {
        setup:
            ReleaserControl releaserControl = new ReleaserControl();
            releaserControl.highSwitchActive = false;
            releaserControl.lowSwitchActive = true;

            def mock = Mock(ProducerTemplate)
            releaserControl.emptyReleaser = mock
        when:
            releaserControl.handleHighSwitchStateChange(true)
        then:
            1 * mock.sendBody("open")
    }

    def "when the high switch is tripped but the releaser is already releasing, no control message is sent"() {
        setup:
            ReleaserControl releaserControl = new ReleaserControl();
            releaserControl.highSwitchActive = false
            releaserControl.lowSwitchActive = true
            releaserControl.releasingInProgress = true

            def mock = Mock(ProducerTemplate)
            releaserControl.emptyReleaser = mock
        when:
            releaserControl.handleHighSwitchStateChange(true)
        then:
            0 * mock.sendBody(_)
    }

    def "when the low switch is no longer active when the releaser is in progress, a control message is sent"() {
        setup:
            ReleaserControl releaserControl = new ReleaserControl();
            releaserControl.highSwitchActive = false
            releaserControl.lowSwitchActive = true
            releaserControl.releasingInProgress = true

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
            releaserControl.highSwitchActive = false
            releaserControl.lowSwitchActive = true
            releaserControl.releasingInProgress = false
            def mock = Mock(ProducerTemplate)
            releaserControl.emptyReleaser = mock
        when:
            releaserControl.handleLowSwitchStateChange(false)
        then:
            0 * mock.sendBody(_)

    }

}
