package com.ryebrye.releaser.statistics;

import java.time.LocalDateTime;

/**
 * @author Ryan Gardner
 * @date 1/3/15
 */
public class ReleaserStatistics {
    private LocalDateTime startDate;
    private LocalDateTime mostRecentReleaseEvent;
    private int countForToday;
    private int countTotal;

    public ReleaserStatistics(LocalDateTime startDate, LocalDateTime mostRecentReleaseEvent, int countForToday, int countTotal) {
        this.startDate = startDate;
        this.mostRecentReleaseEvent = mostRecentReleaseEvent;
        this.countForToday = countForToday;
        this.countTotal = countTotal;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getMostRecentReleaseEvent() {
        return mostRecentReleaseEvent;
    }

    public int getCountForToday() {
        return countForToday;
    }

    public int getCountTotal() {
        return countTotal;
    }
}
