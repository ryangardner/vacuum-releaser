package com.ryebrye.releaser.historical

import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/**
 * @author Ryan Gardner
 * @date 3/31/15
 */
class TwitterMessageGeneratorSpec extends Specification {

    def "test the words make sense"() {
        setup:
            TwitterMessageGenerator messageGenerator = new TwitterMessageGenerator()

            Instant fixedInstant = Instant.parse("2014-03-06T06:01:01Z")
            Clock fixedClock = Clock.fixed(fixedInstant, ZoneId.of(ZoneId.SHORT_IDS.get("EST")))
            ZonedDateTime now = ZonedDateTime.now(fixedClock)

        when:
            def message = messageGenerator.getTimeBetweenEvents(new ReleaserEvent(startTime: now.minusSeconds(5), endTime: now), new ReleaserEvent(startTime: now.minusMinutes(1).minusSeconds(38), endTime: now.minusMinutes(1).minusSeconds(30).minusNanos(TimeUnit.MILLISECONDS.toNanos(100))))
        then:
            message == '1 minute 30 seconds'
    }
}
