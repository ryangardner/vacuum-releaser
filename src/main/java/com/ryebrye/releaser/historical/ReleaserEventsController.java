package com.ryebrye.releaser.historical;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
        return releaserEventRepository.findAll();
    }

    @RequestMapping(value = "/releaserEventsCsv", produces = "text/csv")
    public List<ReleaserEvent> releaserEventsCsv() {
        return releaserEventRepository.findAll();
    }
}
