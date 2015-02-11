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
    private double gallonsForToday;
    private double gallonsTotal;
    private int countTotal;

    public ReleaserStatistics(ZonedDateTime startDate, ZonedDateTime mostRecentReleaseEvent, double gallonsForToday, double gallonsTotal, int countForToday, int countTotal) {
        this.startDate = startDate;
        this.mostRecentReleaseEvent = mostRecentReleaseEvent;
        this.countForToday = countForToday;
        this.gallonsForToday = gallonsForToday;
        this.gallonsTotal = gallonsTotal;
        this.countTotal = countTotal;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public ZonedDateTime getMostRecentReleaseEvent() {
        return mostRecentReleaseEvent;
    }

    public double getGallonsForToday() {
        return gallonsForToday;
    }

    public double getGallonsTotal() {
        return gallonsTotal;
    }

    public int getCountForToday() {
        return countForToday;
    }

    public int getCountTotal() {
        return countTotal;
    }
}
