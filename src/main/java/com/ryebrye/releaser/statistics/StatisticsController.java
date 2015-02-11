package com.ryebrye.releaser.statistics;

import com.ryebrye.releaser.historical.ReleaserEvent;
import com.ryebrye.releaser.historical.ReleaserEventRepository;
import com.ryebrye.releaser.historical.ReleaserEventSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.RequestMapping;

import static java.util.stream.Collectors.summingDouble;

/**
 * @author Ryan Gardner
 * @date 1/3/15
 */
@RestController
public class StatisticsController {

    @Autowired
    private ReleaserEventRepository releaserEventRepository;

    @RequestMapping("/basicStats")
    public ReleaserStatistics releaserStatistics() {
        ZonedDateTime startDate = ZonedDateTime.now().minusNanos(TimeUnit.MILLISECONDS.toNanos(ManagementFactory.getRuntimeMXBean().getUptime()));
        ReleaserEvent mostRecentEvent = releaserEventRepository.findMostRecentCompletedEvent();
        Specification<ReleaserEvent> todaysEventsSpec = ReleaserEventSpecifications.eventsOfDay(LocalDate.now());
        int countForToday = (int) releaserEventRepository.count(todaysEventsSpec);
        Specification<ReleaserEvent> completedEvents = ReleaserEventSpecifications.completedEvents();
        int countTotal = (int) releaserEventRepository.count(completedEvents);
        double gallonsForToday = (double)((List<ReleaserEvent>)releaserEventRepository.findAll(todaysEventsSpec)).stream().filter( re -> re.getSapQuantity() != null).collect(Collectors.summingDouble(ReleaserEvent::getSapQuantity));
        double gallonsTotal  = (double)((List<ReleaserEvent>)releaserEventRepository.findAll(completedEvents)).stream().filter( re -> re.getSapQuantity() != null).collect(Collectors.summingDouble(ReleaserEvent::getSapQuantity));

        return new ReleaserStatistics(startDate, mostRecentEvent != null ? mostRecentEvent.getEndTime() : null, gallonsForToday, gallonsTotal, countForToday, countTotal);
    }

}
