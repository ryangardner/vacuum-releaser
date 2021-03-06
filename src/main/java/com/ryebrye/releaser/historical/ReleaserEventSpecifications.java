package com.ryebrye.releaser.historical;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @author Ryan Gardner
 * @date 1/2/15
 */
public class ReleaserEventSpecifications {

    public static Specification<ReleaserEvent> eventsOfDay(LocalDate time) {
        return new Specification<ReleaserEvent>() {
            @Override
            public Predicate toPredicate(Root<ReleaserEvent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                //query.from(ReleaserEvent.class);
                Path<ZonedDateTime> releaserDate = root.<ZonedDateTime>get("startTime");
                return cb.and(cb.greaterThanOrEqualTo(releaserDate, time.atStartOfDay().atZone(ZoneId.systemDefault())), cb.lessThan(releaserDate, time.atStartOfDay().plusDays(1).atZone(ZoneId.systemDefault())));
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
