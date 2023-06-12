package com.couchbase;


import com.couchbase.client.core.error.CollectionExistsException;
import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.core.error.IndexExistsException;
import com.couchbase.client.core.error.ScopeExistsException;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.manager.collection.CollectionManager;
import com.couchbase.client.java.manager.collection.CollectionSpec;
import com.couchbase.client.java.manager.query.CreatePrimaryQueryIndexOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.stereotype.Component;


/**
 * This class run after the application startup. It automatically sets up all needed indexes
 */

@Component
public class DBSetupRunners implements CommandLineRunner {
    private static final Logger LOG = LoggerFactory.getLogger(DBSetupRunners.class);

    public static String FOO_COLLECTION_NAME = "foo";

    private Cluster cluster;

    private CouchbaseTemplate couchbaseTemplate;
    String bucketName;
    String scopeName;

    public DBSetupRunners(CouchbaseTemplate couchbaseTemplate) {
        this.couchbaseTemplate = couchbaseTemplate;
        this.cluster = couchbaseTemplate.getCouchbaseClientFactory().getCluster();
        this.bucketName = couchbaseTemplate.getBucketName();
        this.scopeName = couchbaseTemplate.getScopeName();
    }

    @Override
    public void run(String... args) {
        CollectionManager collectionManager = cluster.bucket(bucketName).collections();

        try {
            collectionManager.createScope("Couchbase");
            LOG.info("Created Couchbase Scope for bucket " + bucketName);
        } catch (ScopeExistsException e) {
            LOG.info("Couchbase Scope exists on bucket " + bucketName);
        } catch (CouchbaseException e) {
            LOG.error("Something wrong with scope creation for bucket " + bucketName);
            e.printStackTrace();
        }

        createCollection(collectionManager, FOO_COLLECTION_NAME);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        createCollectionPrimaryIndex(FOO_COLLECTION_NAME);

        LOG.error("Application is ready");
    }

    private void createCollection(CollectionManager collectionManager, String collectionName) {
        try {
            CollectionSpec spec = CollectionSpec.create(collectionName, scopeName);
            collectionManager.createCollection(spec);
            LOG.info("Created collection '" + spec.name() + "' in scope '" + spec.scopeName() + "' of bucket '" + bucketName + "'");
        } catch (CollectionExistsException e) {
            LOG.info(String.format("Collection <%s> already exists", collectionName));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createCollectionPrimaryIndex(String collectionName) {
        try {
            CreatePrimaryQueryIndexOptions options = CreatePrimaryQueryIndexOptions.createPrimaryQueryIndexOptions()
                    .scopeName(scopeName)
                    .collectionName(collectionName);
            cluster.queryIndexes().createPrimaryIndex(bucketName, options);
            LOG.info("Created primary index for " + bucketName + " " + collectionName);
        } catch (IndexExistsException e) {
            LOG.info("Index already exist for " + bucketName + " " + collectionName);
        } catch (CouchbaseException e) {
            throw new RuntimeException(e);
        }
    }
}
