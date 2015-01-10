package com.ryebrye.releaser.statistics;

import com.ryebrye.releaser.historical.ReleaserEvent;
import com.ryebrye.releaser.historical.ReleaserEventRepository;
import com.ryebrye.releaser.historical.ReleaserEventSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import org.springframework.web.bind.annotation.RequestMapping;

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
        int countForToday = (int) releaserEventRepository.count(ReleaserEventSpecifications.eventsOfDay(LocalDate.now()));
        int countTotal = (int) releaserEventRepository.count(ReleaserEventSpecifications.completedEvents());

        return new ReleaserStatistics(startDate, mostRecentEvent != null ? mostRecentEvent.getEndTime() : null, countForToday, countTotal);
    }

}
