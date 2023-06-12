package com.couchbase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.couchbase.core.mapping.CouchbaseDocument;
import org.springframework.data.couchbase.core.mapping.event.AbstractCouchbaseEventListener;

import java.util.concurrent.atomic.AtomicInteger;

class CustomListener extends AbstractCouchbaseEventListener<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractCouchbaseEventListener.class);

    public static AtomicInteger counter = new AtomicInteger(0);

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void onBeforeSave(Object source, CouchbaseDocument doc) {
        float amount = (float) doc.get("amount");
        if (amount > 100) {
            counter.getAndIncrement();
            LOG.info("Amount greater thab 100 spotted: {}", source);
        }
    }

}