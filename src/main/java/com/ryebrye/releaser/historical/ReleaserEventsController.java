package com.ryebrye.releaser.historical;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Ryan Gardner
 * @date 1/4/15
 */
@RestController
public class ReleaserEventsController {
    @Autowired
    protected ReleaserEventRepository releaserEventRepository;

    @RequestMapping("/releaserEvents")
    public List<ReleaserEvent> releaserEvents() {
        List<ReleaserEvent> releaserEvents = releaserEventRepository.findAll();
        releaserEvents.forEach((ReleaserEvent event) -> {
                    if (event.getSapQuantity() == null) {
                        event.setSapQuantity(1.2);
                    }
                    if (event.getTemperature() == null) {
                        event.setTemperature(ThreadLocalRandom.current().nextDouble(32, 60));
                    }
                }
        );
        return releaserEvents;
    }

}
