package com.ryebrye.releaser;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @author Ryan Gardner
 * @date 2/8/15
 */
@Entity
public class ReleaserSettings {

    // single row table
    @Id
    private Long id=0l;

    private Double  gallonsPerFullDump;

    private Integer numberOfTaps;

    public Long getId() {
        return id;
    }

    public Double getGallonsPerFullDump() {
        return gallonsPerFullDump;
    }

    public void setGallonsPerFullDump(Double sapQuantityPerFullDump) {
        this.gallonsPerFullDump = sapQuantityPerFullDump;
    }

    public Integer getNumberOfTaps() {
        return numberOfTaps;
    }

    public void setNumberOfTaps(Integer numberOfTaps) {
        this.numberOfTaps = numberOfTaps;
    }

    public static ReleaserSettings withDefaults() {
        ReleaserSettings releaserSettings = new ReleaserSettings();
        releaserSettings.setNumberOfTaps(100);
        releaserSettings.setGallonsPerFullDump(1.01);
        return releaserSettings;
    }
}
