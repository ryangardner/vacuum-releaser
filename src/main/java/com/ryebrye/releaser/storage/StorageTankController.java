package com.ryebrye.releaser.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Ryan Gardner
 * @date 3/12/15
 */
@RestController
public class StorageTankController {

    @Autowired
    public StorageTankRepository storageTankRepository;

    @RequestMapping(value = "/storageTank", method = RequestMethod.GET)
    public StorageTank releaserSettings() {
        return storageTankRepository.findStorageTank();
    }

    @RequestMapping(value = "/storageTank", method = RequestMethod.POST)
    public StorageTank save(@RequestBody StorageTank storageTank) {
        return storageTankRepository.saveAndFlush(storageTank);
    }
}
