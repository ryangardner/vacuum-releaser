package com.ryebrye.releaser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Ryan Gardner
 * @date 2/8/15
 */
@RestController
public class ReleaserSettingsController {

    @Autowired
    public ReleaserSettingsRepository releaserSettingsRepository;

    @RequestMapping(value="/settings", method= RequestMethod.GET)
    public ReleaserSettings releaserSettings() {
        ReleaserSettings savedSettings = releaserSettingsRepository.findOne(0l);
        return savedSettings != null ? savedSettings : ReleaserSettings.withDefaults();
    }

    @RequestMapping(value="/settings", method= RequestMethod.POST)
    public void saveReleaserSettings(@RequestBody ReleaserSettings releaserSettings) {
        releaserSettingsRepository.saveAndFlush(releaserSettings);
    }
}
