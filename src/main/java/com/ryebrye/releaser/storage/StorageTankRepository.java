package com.ryebrye.releaser.storage;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Ryan Gardner
 * @date 3/12/15
 */
public interface StorageTankRepository extends JpaRepository<StorageTank, Long> {
    @Override
    @CacheEvict(value = "storageTank", allEntries = true)
    <S extends StorageTank> List<S> save(Iterable<S> entities);

    @Override
    @CacheEvict(value = "storageTank", allEntries = true)
    <S extends StorageTank> S saveAndFlush(S entity);

    @Override
    @Cacheable("storageTank")
    StorageTank findOne(Long aLong);

    @Cacheable("storageTank")
    default StorageTank findStorageTank() {
        StorageTank storageTank = findOne(0l);
        if (storageTank == null) {
            return StorageTank.withDefaults();
        } else {
            return storageTank;
        }

    }
}
