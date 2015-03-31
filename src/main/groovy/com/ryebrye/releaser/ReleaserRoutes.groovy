package com.ryebrye.releaser

import com.ryebrye.releaser.historical.ReleaserEvent
import com.ryebrye.releaser.historical.ReleaserEventRepository
import com.ryebrye.releaser.historical.ReleaserEventSpecifications
import com.ryebrye.releaser.historical.TwitterMessageGenerator
import com.ryebrye.releaser.storage.StorageTank
import com.ryebrye.releaser.storage.StorageTankRepository
import groovy.transform.CompileStatic
import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.twitter.TwitterComponent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import java.time.LocalDate
import java.time.ZonedDateTime

/**
 * @author Ryan Gardner
 * @date 12/31/14
 */
@Component
@CompileStatic
class ReleaserRoutes extends RouteBuilder {
    @Value('${twitter.consumerApiKey}')
    private String consumerKey

    @Value('${twitter.consumerSecret}')
    private String consumerSecret

    @Value('${twitter.accessToken}')
    private String accessToken

    @Value('${twitter.accessTokenSecret}')
    private String accessTokenSecret


    @Autowired
    private ReleaserEventRepository releaserEventRepository

    @Autowired
    private TwitterMessageGenerator twitterMessageGenerator

    @Autowired
    private StorageTankRepository storageTankRepository


    static final Logger log = LoggerFactory.getLogger(ReleaserRoutes)

    @Override
    void configure() throws Exception {
        log.info("Configuring routes")

        if (accessToken != null && accessTokenSecret != null && consumerSecret != null && consumerKey)
            configureTwitterComponent()

        from("seda:releaserControl")
                .routeId("releaserControl")
                .multicast().to("seda:releaserHardwareControl", "seda:releaserSoftware")

        from("seda:releaserSoftware")
                .choice()
                .when(body().isEqualTo("close"))
                .to("direct:releaserClosing")
                .otherwise()
                .to("direct:releaserOpening")
                .endChoice()

        from("direct:releaserOpening").routeId("createEvent")
        //.setBody { new ReleaserEvent(startTime: ZonedDateTime.now()) }
                .to("seda:saveReleaserEvent")

        from("direct:releaserClosing").routeId("updateEvent")
                .beanRef("releaserEventRepository", "findMostRecentUnfinishedEvent")
                .process { Exchange it ->
            (it.in.body as ReleaserEvent).endTime = ZonedDateTime.now()
        }
        .process { Exchange it ->
            it.in.headers.put('lastEvent', releaserEventRepository.findMostRecentCompletedEvent() != null ? releaserEventRepository.findMostRecentCompletedEvent() : it.in.body)
        }
        .multicast().to("seda:saveReleaserEvent", "seda:tweetAboutIt", "seda:incrementSapInTank")

        from("seda:incrementSapInTank")
                .process({ Exchange it ->
            StorageTank tank = storageTankRepository.findStorageTank()
            tank.addSap((it.in.body as ReleaserEvent).sapQuantity)
            storageTankRepository.saveAndFlush(tank);
        })


        from("seda:tweetAboutIt").routeId("tweet")
                .transform({ Exchange it ->
            "More sap! That makes ${releaserEventRepository.count(ReleaserEventSpecifications.eventsOfDay(LocalDate.now()))}" +
                    " time${releaserEventRepository.count(ReleaserEventSpecifications.eventsOfDay(LocalDate.now())) > 1 ? "s" : ""} for today." +
                    " (Temp is ${sprintf('%2.1f', (it.in.body as ReleaserEvent).temperature)}°F - " +
                    "took ${twitterMessageGenerator.getTimeBetweenEvents(it.in.body as ReleaserEvent, it.in.getHeader("lastEvent") as ReleaserEvent)} for this cycle)" as String
        })
        //.process({Exchange it -> log.info(it.in.body as String)})
                .to("twitter://timeline/user")

        // split this part out so we can easily use the BAM monitoring on this
        from("seda:saveReleaserEvent")
                .beanRef("releaserEventRepository", "save")
                .to("log:loggingReleasing")


    }

    public void configureTwitterComponent() {
        TwitterComponent tc = getContext().getComponent("twitter", TwitterComponent.class);
        tc.setAccessToken(accessToken);
        tc.setAccessTokenSecret(accessTokenSecret);
        tc.setConsumerKey(consumerKey);
        tc.setConsumerSecret(consumerSecret);
    }

}
