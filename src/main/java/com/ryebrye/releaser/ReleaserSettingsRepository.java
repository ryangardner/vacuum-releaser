package com.ryebrye.releaser;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

/**
 * @author Ryan Gardner
 * @date 2/8/15
 */
public interface ReleaserSettingsRepository  extends JpaRepository<ReleaserSettings, Long> {

    @Override
    @CacheEvict(value="settingsCache", allEntries = true)
    <S extends ReleaserSettings> List<S> save(Iterable<S> entities);

    @Override
    @CacheEvict(value="settingsCache", allEntries = true)
    <S extends ReleaserSettings> S saveAndFlush(S entity);

    @Override
    @Cacheable("settingsCache")
    ReleaserSettings findOne(Long aLong);
}
