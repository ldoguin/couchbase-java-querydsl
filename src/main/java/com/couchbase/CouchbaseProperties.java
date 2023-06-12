package com.couchbase;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "couchbase")
public record CouchbaseProperties(String connectionString, String username, String password, String defaultBucket,String defaultCollection, String encryptionKey) {
}