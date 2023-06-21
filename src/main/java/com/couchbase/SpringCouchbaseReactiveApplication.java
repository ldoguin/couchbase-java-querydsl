package com.couchbase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SpringCouchbaseReactiveApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringCouchbaseReactiveApplication.class, args);
    }
}
