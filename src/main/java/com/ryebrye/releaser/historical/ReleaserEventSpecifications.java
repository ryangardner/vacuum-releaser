package com.ryebrye.releaser.historical;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.ZonedDateTime;

/**
 * @author Ryan Gardner
 * @date 1/2/15
 */
public class ReleaserEventSpecifications {

    public static Specification<ReleaserEvent> eventsOfDay(ZonedDateTime time) {
        return new Specification<ReleaserEvent>() {
            @Override
            public Predicate toPredicate(Root<ReleaserEvent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                //query.from(ReleaserEvent.class);
                Path<ZonedDateTime> releaserDate = root.<ZonedDateTime>get("startTime");
                ZonedDateTime midnightOfDay = time.withHour(0).withMinute(0).withSecond(0).withNano(0);
                return cb.and(cb.greaterThanOrEqualTo(releaserDate, midnightOfDay), cb.lessThan(releaserDate, midnightOfDay.plusDays(1).minusNanos(1)));
            }
        };
    }

    public static Specification<ReleaserEvent> completedEvents() {
        return new Specification<ReleaserEvent>() {
            @Override
            public Predicate toPredicate(Root<ReleaserEvent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                //query.from(ReleaserEvent.class);
                Path<ZonedDateTime> startTime = root.<ZonedDateTime>get("startTime");
                Path<ZonedDateTime> endTime = root.<ZonedDateTime>get("endTime");
                return cb.and(cb.isNotNull(startTime), cb.isNotNull(endTime));
            }
        };
    }

}
