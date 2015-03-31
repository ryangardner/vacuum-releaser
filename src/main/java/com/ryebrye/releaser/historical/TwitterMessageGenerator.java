package com.ryebrye.releaser.historical;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.springframework.stereotype.Component;

import java.time.Duration;


/**
 * @author Ryan Gardner
 * @date 3/31/15
 */
@Component
public class TwitterMessageGenerator {

    public String getTimeBetweenEvents(ReleaserEvent event1, ReleaserEvent event2) {
        Duration duration = Duration.between(event1.getEndTime(), event2.getEndTime()).abs();
        return DurationFormatUtils.formatDurationWords(duration.toMillis(), true, true);
    }
}
