package com.ryebrye.releaser.historical;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * Created by ryangardner on 12/30/14.
 */
public interface ReleaserEventRepository extends JpaRepository<ReleaserEvent, Long>, JpaSpecificationExecutor {

    // has to be a native query because HQL doesn't support the 'limit' keyword
    @Query(value = "select * from releaser_event as e where e.start_time is not null and e.end_time is null order by e.start_time desc limit 1", nativeQuery = true)
    public ReleaserEvent findMostRecentUnfinishedEvent();

    @Query(value = "select * from releaser_event as e where e.start_time is not null and e.end_time is not null order by e.start_time desc limit 1", nativeQuery = true)
    public ReleaserEvent findMostRecentCompletedEvent();

}
