package com.couchbase;

import com.couchbase.client.core.msg.kv.DurabilityLevel;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.query.QueryScanConsistency;
import com.couchbase.client.java.transactions.config.TransactionsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.repository.auditing.EnableReactiveCouchbaseAuditing;
import org.springframework.data.couchbase.repository.config.EnableReactiveCouchbaseRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.Duration;

@Configuration
@EnableReactiveCouchbaseAuditing
@EnableReactiveCouchbaseRepositories
@EnableTransactionManagement
public class CouchbaseConfiguration extends AbstractCouchbaseConfiguration {

    @Autowired
    CouchbaseProperties clusterProperties;

    @Override
    public String getConnectionString() {
        return clusterProperties.connectionString();
    }

    @Override
    public String getUserName() {
        return clusterProperties.username();
    }

    @Override
    public String getPassword() {
        return clusterProperties.password();
    }

    @Override
    public String getBucketName() {
        return clusterProperties.defaultBucket();
    }

    @Override
    protected boolean autoIndexCreation() {
        return true;
    }

    @Override
    public QueryScanConsistency getDefaultConsistency() {
        return QueryScanConsistency.REQUEST_PLUS;
    }

    @Override
    protected void configureEnvironment(final ClusterEnvironment.Builder builder) {
        builder.timeoutConfig().kvTimeout(Duration.ofSeconds(2));

        builder.transactionsConfig(
                TransactionsConfig.builder()
                        // Set durability level to nonw as testing on a one node cluster.
                        .durabilityLevel(DurabilityLevel.NONE)
                        .timeout(Duration.ofSeconds(30)));
    }

    @Bean
    public CustomListener customListener() {
        return new CustomListener();
    }
}
