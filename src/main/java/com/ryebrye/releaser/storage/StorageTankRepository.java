package com.ryebrye.releaser.storage;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Ryan Gardner
 * @date 3/12/15
 */
public interface StorageTankRepository extends JpaRepository<StorageTank, Long> {

    @Override
    @CacheEvict(value = "storageTank", key = "0")
    <S extends StorageTank> S save(S s);

    @Override
    @CacheEvict(value = "storageTank", key = "0")
    <S extends StorageTank> S saveAndFlush(S entity);

    @Override
    @Cacheable(value="storageTank", key ="0")
    StorageTank findOne(Long aLong);

    @Cacheable(value = "storageTank", key="0")
    default StorageTank findStorageTank() {
        StorageTank storageTank = findOne(0l);
        if (storageTank == null) {
            return StorageTank.withDefaults();
        } else {
            return storageTank;
        }

    }
}
