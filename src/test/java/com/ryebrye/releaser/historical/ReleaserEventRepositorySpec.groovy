package com.ryebrye.releaser.historical

import com.ryebrye.releaser.ReleaserApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import java.time.ZonedDateTime

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
            ZonedDateTime now = ZonedDateTime.now()
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
            ZonedDateTime now = ZonedDateTime.now()
            releaserEventRepository.save(new ReleaserEvent(startTime: now.minusMinutes(2)))
            releaserEventRepository.save(new ReleaserEvent(startTime: now.minusMinutes(1)))
            releaserEventRepository.save(new ReleaserEvent(startTime: now))
            releaserEventRepository.save(new ReleaserEvent(startTime: now.plusDays(1)))
            releaserEventRepository.save(new ReleaserEvent(startTime: now.plusDays(1).minusMinutes(1)))
            releaserEventRepository.save(new ReleaserEvent(startTime: now.minusDays(1)))
        when:
            def events = releaserEventRepository.findAll(ReleaserEventSpecifications.eventsOfDay(now))
        then:
            events.size() == 3
        when:
            events = releaserEventRepository.findAll(ReleaserEventSpecifications.eventsOfDay(now.plusDays(1)))
        then:
            events.size() == 2
        when:
            events = releaserEventRepository.findAll(ReleaserEventSpecifications.eventsOfDay(now.minusDays(1)))
        then:
            events.size() == 1
        cleanup:
            releaserEventRepository.deleteAllInBatch()
    }

    def "the most recent unfinished query can be retrieved by calling the method on the repository when there is only one without an end time"() {
        setup:
            ZonedDateTime now = ZonedDateTime.now()
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
            ZonedDateTime now = ZonedDateTime.now()
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


}