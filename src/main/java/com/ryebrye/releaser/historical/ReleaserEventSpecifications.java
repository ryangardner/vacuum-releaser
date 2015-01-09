package com.ryebrye.releaser.historical;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
                Path<LocalDateTime> releaserDate = root.<LocalDateTime>get("startTime");
                return cb.and(cb.greaterThanOrEqualTo(releaserDate, time.atStartOfDay()), cb.lessThan(releaserDate, time.atStartOfDay().plusDays(1)));
            }
        };
    }

    public static Specification<ReleaserEvent> completedEvents() {
        return new Specification<ReleaserEvent>() {
            @Override
            public Predicate toPredicate(Root<ReleaserEvent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                //query.from(ReleaserEvent.class);
                Path<LocalDateTime> startTime = root.<LocalDateTime>get("startTime");
                Path<LocalDateTime> endTime = root.<LocalDateTime>get("endTime");
                return cb.and(cb.isNotNull(startTime), cb.isNotNull(endTime));
            }
        };
    }

}
