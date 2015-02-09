package com.ryebrye.releaser;

import org.h2.mvstore.MVStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.Arrays;

@Configuration
@ComponentScan
@EnableCaching
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = {"com.ryebrye.releaser.historical", "com.ryebrye.releaser", "org.apache.camel.bam.model"})
@ImportResource("classpath:camel-context.xml")
public class ReleaserApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReleaserApplication.class, args);
    }

    @Bean
    @DependsOn("releaserControl")
    public ReleaserManagement releaserManagement() {
        return new ReleaserManagement();
    }

    // ignore unresolveable placeholders so that you can get by not passing in the twitter properties
    @Bean
    public PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setIgnoreUnresolvablePlaceholders(true);
        return configurer;
    }


    @Bean
   	public CacheManager cacheManager() {

   		Cache cache = new ConcurrentMapCache("settingsCache");

   		SimpleCacheManager manager = new SimpleCacheManager();
   		manager.setCaches(Arrays.asList(cache));

   		return manager;
   	}

    @Profile("!test")
    @Bean
    public DataSource realDatabase() {
        DriverManagerDataSource h2dbDatasource = new DriverManagerDataSource();
        h2dbDatasource.setDriverClassName("org.h2.Driver");
        h2dbDatasource.setUrl("jdbc:h2:~/releaserDb");
        h2dbDatasource.setUsername("sa");
        h2dbDatasource.setPassword("");
        return h2dbDatasource;
    }

    @Profile("test")
    @Bean
    public DataSource testDatabase() {
        DriverManagerDataSource h2dbDatasource = new DriverManagerDataSource();
        h2dbDatasource.setDriverClassName("org.h2.Driver");
        h2dbDatasource.setUrl("jdbc:h2:mem:releaserDb;DB_CLOSE_DELAY=-1");
        h2dbDatasource.setUsername("sa");
        h2dbDatasource.setPassword("");
        return h2dbDatasource;
    }

}
