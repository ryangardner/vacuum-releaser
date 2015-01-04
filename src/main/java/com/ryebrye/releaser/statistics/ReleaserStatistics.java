package com.ryebrye.releaser.statistics;

import java.time.ZonedDateTime;

/**
 * @author Ryan Gardner
 * @date 1/3/15
 */
public class ReleaserStatistics {
    private ZonedDateTime startDate;
    private ZonedDateTime mostRecentReleaseEvent;
    private int countForToday;
    private int countTotal;

    public ReleaserStatistics(ZonedDateTime startDate, ZonedDateTime mostRecentReleaseEvent, int countForToday, int countTotal) {
        this.startDate = startDate;
        this.mostRecentReleaseEvent = mostRecentReleaseEvent;
        this.countForToday = countForToday;
        this.countTotal = countTotal;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public ZonedDateTime getMostRecentReleaseEvent() {
        return mostRecentReleaseEvent;
    }

    public int getCountForToday() {
        return countForToday;
    }

    public int getCountTotal() {
        return countTotal;
    }
}
