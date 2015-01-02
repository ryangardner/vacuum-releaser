package com.ryebrye.releaser

import com.ryebrye.releaser.historical.ReleaserEvent
import groovy.transform.CompileStatic
import org.apache.camel.Consume
import org.apache.camel.builder.RouteBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

import java.time.ZonedDateTime

/**
 * @author Ryan Gardner
 * @date 12/31/14
 */
@Component
@CompileStatic
class ReleaserRoutes extends RouteBuilder {

    static final Logger log = LoggerFactory.getLogger(ReleaserRoutes)

    @Override
    void configure() throws Exception {
        log.info("Configuring routes")

        from("seda:releaserControl")
                .routeId("releaserControl")
                .multicast().to("seda:releaserHardwareControl","seda:releaserSoftware")

        from("seda:releaserSoftware")
            .choice()
                .when(body().isEqualTo("open"))
                    .to("direct:releaserOpening")
                .otherwise()
                    .to("direct:releaserClosing")
                .endChoice()

        from("direct:releaserOpening")
            .setBody{new ReleaserEvent(startTime: ZonedDateTime.now())}
            .beanRef("releaserEventRepository", "save")
            .to("log:loggingReleasing")
    }
}
