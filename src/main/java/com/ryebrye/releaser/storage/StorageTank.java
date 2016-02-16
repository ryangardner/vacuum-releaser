package com.ryebrye.releaser.storage;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;

/**
 * @author Ryan Gardner
 * @date 3/12/15
 */
@Entity
public class StorageTank {

    // single row table
    @Id
    private Long id = 0l;

    private double currentVolume;
    private double capacity;
    private double warningThreshold;

    public double getCurrentVolume() {
        return currentVolume;
    }

    public void setCurrentVolume(double currentVolume) {
        this.currentVolume = currentVolume;
    }

    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public double getWarningThreshold() {
        return warningThreshold;
    }

    public StorageTank setWarningThreshold(double warningThreshold) {
        this.warningThreshold = warningThreshold;
        return this;
    }

    public StorageTank addSap(double volume) {
        this.currentVolume += volume;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StorageTank)) {
            return false;
        }
        StorageTank that = (StorageTank) o;
        return Double.compare(that.currentVolume, currentVolume) == 0 &&
                Double.compare(that.capacity, capacity) == 0 &&
                Double.compare(that.warningThreshold, warningThreshold) == 0 &&
                Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, currentVolume, capacity, warningThreshold);
    }

    public StorageTank removeSap(double volume) {
        this.currentVolume -= volume;
        return this;
    }

    public static StorageTank withDefaults() {
        StorageTank storageTank = new StorageTank();
        storageTank.setCurrentVolume(0.0);
        storageTank.setCapacity(200.0);
        storageTank.setWarningThreshold(180.0);
        storageTank.id = 0l;
        return storageTank;
    }


}
