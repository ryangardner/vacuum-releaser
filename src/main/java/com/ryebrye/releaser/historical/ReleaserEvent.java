package com.ryebrye.releaser.historical;

import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.ZonedDateTime;

/**
 * Created by ryangardner on 12/30/14.
 */
@Table(indexes = {@Index(name="start_time_index", columnList = "start_time"), @Index(name="end_time_index", columnList = "end_time")})
@Entity
public class ReleaserEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @CreatedDate
    @Column(name = "start_time")
    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentZonedDateTime")
    private ZonedDateTime startTime;

    @Column(name = "end_time")
    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentZonedDateTime")
    private ZonedDateTime endTime;

    @Column(name="sap_qty_gal")
    private Double sapQuantity;

    public Duration getDuration() {
        if (endTime == null) {
            return Duration.between(ZonedDateTime.now(), startTime).abs();
        }
        return Duration.between(endTime, startTime).abs();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(ZonedDateTime endTime) {
        this.endTime = endTime;
    }

    public Double getSapQuantity() {
        return sapQuantity;
    }

    public void setSapQuantity(Double sapQuantity) {
        this.sapQuantity = sapQuantity;
    }

    @Override
    public String toString() {
        return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("startTime", startTime)
                .add("endTime", endTime)
                .add("sapQty", sapQuantity)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReleaserEvent)) {
            return false;
        }

        ReleaserEvent that = (ReleaserEvent) o;

        if (id != that.id) {
            return false;
        }
        if (endTime != null ? !endTime.equals(that.endTime) : that.endTime != null) {
            return false;
        }
        if (sapQuantity != null ? !sapQuantity.equals(that.sapQuantity) : that.sapQuantity != null) {
            return false;
        }
        if (!startTime.equals(that.startTime)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + startTime.hashCode();
        result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
        result = 31 * result + (sapQuantity != null ? sapQuantity.hashCode() : 0);
        return result;
    }
}
