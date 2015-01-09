package com.ryebrye.releaser.historical

import com.ryebrye.releaser.ReleaserApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.LocalDateTime
import java.util.function.Function

/**
 * @author Ryan Gardner
 * @date 1/2/15
 */
@ActiveProfiles("test")
@ContextConfiguration(classes = [ReleaserApplication])
class ReleaserEventRepositorySpec extends Specification {


    @Autowired
    ReleaserEventRepository releaserEventRepository

    def "after saving three events, the findAll() method returns 3 elements"() {
        setup:
            LocalDateTime now = LocalDateTime.now()
            releaserEventRepository.save(new ReleaserEvent(startTime: now.minusMinutes(2)))
            releaserEventRepository.save(new ReleaserEvent(startTime: now.minusMinutes(1)))
            releaserEventRepository.save(new ReleaserEvent(startTime: now))
        when:
            def events = releaserEventRepository.findAll()
        then:
            events.size() == 3
        cleanup:
            releaserEventRepository.deleteAllInBatch()
    }

    def "the specification query 'eventsOfDay' is able to retrieve events from only a single day"() {
        setup:
            LocalDateTime now = LocalDateTime.now()
            releaserEventRepository.save(new ReleaserEvent(startTime: now.minusMinutes(2)))
            releaserEventRepository.save(new ReleaserEvent(startTime: now.minusMinutes(1)))
            releaserEventRepository.save(new ReleaserEvent(startTime: now))
            releaserEventRepository.save(new ReleaserEvent(startTime: now.plusDays(1)))
            releaserEventRepository.save(new ReleaserEvent(startTime: now.plusDays(1).minusMinutes(1)))
            releaserEventRepository.save(new ReleaserEvent(startTime: now.minusDays(1)))
        when:
            def events = releaserEventRepository.findAll(ReleaserEventSpecifications.eventsOfDay(now.toLocalDate()))
        then:
            events.size() == 3
        when:
            events = releaserEventRepository.findAll(ReleaserEventSpecifications.eventsOfDay(now.plusDays(1).toLocalDate()))
        then:
            events.size() == 2
        when:
            events = releaserEventRepository.findAll(ReleaserEventSpecifications.eventsOfDay(now.minusDays(1).toLocalDate()))
        then:
            events.size() == 1
        cleanup:
            releaserEventRepository.deleteAllInBatch()
    }

    def "the most recent unfinished query can be retrieved by calling the method on the repository when there is only one without an end time"() {
        setup:
            LocalDateTime now = LocalDateTime.now()
            releaserEventRepository.save(new ReleaserEvent(startTime: now.minusMinutes(2), endTime: now.minusMinutes(1)))
            releaserEventRepository.save(new ReleaserEvent(startTime: now))
        when:
            def event = releaserEventRepository.findMostRecentUnfinishedEvent()
        then:
            event.endTime == null
            event.startTime.isEqual(now)
        cleanup:
            releaserEventRepository.deleteAllInBatch()
    }

    def "the most recent unfinished query only returns one result when there are multiple unfinished events"() {
        setup:
            LocalDateTime now = LocalDateTime.now()
            releaserEventRepository.save(new ReleaserEvent(startTime: now.minusMinutes(2), endTime: now.minusMinutes(1)))
            releaserEventRepository.save(new ReleaserEvent(startTime: now.minusDays(1)))
            releaserEventRepository.save(new ReleaserEvent(startTime: now))
        when:
            def event = releaserEventRepository.findMostRecentUnfinishedEvent()
        then:
            event.endTime == null
            event.startTime.isEqual(now)
        cleanup:
            releaserEventRepository.deleteAllInBatch()
    }


    def "finding events for that specific day returns the correct events"() {
        setup:
            // 1am EST = 6AM UTC
            Instant fixedInstant = Instant.parse("2014-03-06T06:01:01Z")
            Clock fixedClock = Clock.fixed(fixedInstant, ZoneId.of(ZoneId.SHORT_IDS.get("EST")))
            LocalDateTime startingDateTime = LocalDateTime.now(fixedClock)
            releaserEventRepository.save(new ReleaserEvent(startTime: startingDateTime.minusHours(5), endTime: startingDateTime.plusMinutes(2)))
            releaserEventRepository.save(new ReleaserEvent(startTime: startingDateTime.minusHours(4), endTime: startingDateTime.plusMinutes(2)))
            releaserEventRepository.save(new ReleaserEvent(startTime: startingDateTime.plusMinutes(2), endTime: startingDateTime.plusMinutes(2)))
            releaserEventRepository.save(new ReleaserEvent(startTime: startingDateTime.plusHours(1), endTime: startingDateTime.plusHours(1).plusMinutes(1)))
            releaserEventRepository.save(new ReleaserEvent(startTime: startingDateTime.plusHours(5), endTime: startingDateTime.plusHours(5).plusMinutes(1)))
            releaserEventRepository.save(new ReleaserEvent(startTime: startingDateTime.plusHours(10), endTime: startingDateTime.plusHours(10).plusMinutes(1)))
            releaserEventRepository.save(new ReleaserEvent(startTime: startingDateTime.plusHours(18), endTime: startingDateTime.plusHours(18).plusMinutes(1)))
            releaserEventRepository.save(new ReleaserEvent(startTime: startingDateTime.plusHours(22), endTime: startingDateTime.plusHours(22).plusMinutes(1)))
            releaserEventRepository.save(new ReleaserEvent(startTime: startingDateTime.plusHours(30), endTime: startingDateTime.plusHours(30).plusMinutes(1)))

        when:
           def events = releaserEventRepository.findAll(ReleaserEventSpecifications.eventsOfDay(LocalDate.now(fixedClock)))
        then:
            events.size() == 6
        cleanup:
            releaserEventRepository.deleteAllInBatch()

    }

    def "the most recent completed event can be found"() {
        setup:
            LocalDateTime now = LocalDateTime.now()
            releaserEventRepository.save(new ReleaserEvent(startTime: now.minusMinutes(2), endTime: now.minusMinutes(1)))
            releaserEventRepository.save(new ReleaserEvent(startTime: now.minusDays(1)))
            releaserEventRepository.save(new ReleaserEvent(startTime: now))
        when:
            def event = releaserEventRepository.findMostRecentCompletedEvent()
        then:
            event.endTime.isEqual(now.minusMinutes(1))
            event.startTime.isEqual(now.minusMinutes(2))
        cleanup:
            releaserEventRepository.deleteAllInBatch()
    }

    def "a count of completed events can be found using the specification"() {
        setup:
            LocalDateTime now = LocalDateTime.now()
            releaserEventRepository.save(new ReleaserEvent(startTime: now.minusMinutes(2), endTime: now.minusMinutes(1)))
            releaserEventRepository.save(new ReleaserEvent(startTime: now.minusDays(1), endTime: now))
            releaserEventRepository.save(new ReleaserEvent(startTime: now))
        when:
            int event = releaserEventRepository.count(ReleaserEventSpecifications.completedEvents())
        then:
            event == 2
    }


}