package com.ryebrye.releaser.historical;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Created by ryangardner on 12/30/14.
 */
public interface ReleaserEventRepository extends JpaRepository<ReleaserEvent, Long>, JpaSpecificationExecutor  {

}
