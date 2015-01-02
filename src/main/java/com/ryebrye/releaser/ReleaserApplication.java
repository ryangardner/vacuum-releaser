package com.ryebrye.releaser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.JolokiaAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.autoconfigure.web.HttpMessageConvertersAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = "com.ryebrye.releaser.historical")
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

    @Bean
    public DataSource embeddedDatabase() {
        DriverManagerDataSource h2dbDatasource = new DriverManagerDataSource();
        h2dbDatasource.setDriverClassName("org.h2.Driver");
        h2dbDatasource.setUrl("jdbc:h2:nioMapped:~/releaserDb");
        h2dbDatasource.setUsername("sa");
        h2dbDatasource.setPassword("");
        return h2dbDatasource;
    }

}
