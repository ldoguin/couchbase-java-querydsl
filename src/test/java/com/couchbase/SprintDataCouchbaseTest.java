package com.couchbase;

import com.couchbase.client.core.retry.reactor.Retry;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.manager.query.CollectionQueryIndexManager;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryScanConsistency;
import com.couchbase.client.java.transactions.TransactionGetResult;
import com.couchbase.client.java.transactions.TransactionResult;
import com.couchbase.client.java.transactions.error.TransactionCommitAmbiguousException;
import com.couchbase.client.java.transactions.error.TransactionFailedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.data.couchbase.core.mapping.CouchbaseDocument;
import org.springframework.data.couchbase.core.query.Query;
import org.springframework.data.couchbase.core.query.QueryCriteria;
import org.springframework.data.couchbase.transaction.error.TransactionRollbackRequestedException;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;

@SpringBootTest
class SprintDataCouchbaseTest {

    @Autowired
    FooRepository fooRepository;

    @Autowired
    CouchbaseTemplate couchbaseTemplate;

    @Autowired
    TransactionalOperator txOperator;

    List<String> sizes = List.of("s", "m", "l");
    Stream<Foo> document_list = Stream.of(new Foo("Foo", "Bar", 0, sizes), new Foo("Bar", "Bar", -10, sizes), new Foo("Foo", "Foo", 10, sizes), new Foo("Bar", "Foo", 100, sizes));

    public void createFoos(Stream<Foo> document_list) {
        flushBucket();
        document_list.forEach(doc -> fooRepository.save(doc).block());
    }

    public void flushBucket() {
        couchbaseTemplate.getCouchbaseClientFactory().getCluster().buckets().flushBucket(couchbaseTemplate.getBucketName());
    }

    @Test
    void testSpringQueryCriteria() {
        createFoos(document_list);

        Query q = new Query().addCriteria(QueryCriteria.where("firstname").eq("Foo"));
        List<Foo> foos = couchbaseTemplate.findByQuery(Foo.class).matching(q).all();

        List<FooView> foosView = couchbaseTemplate.findByQuery(Foo.class).as(FooView.class).matching(q).all();
        /***
         There is a Filter on "_class": "com.couchbase.Foo" because object where stored with this field as metadata
         since collection where not available in Couchbase. You can however use the
         {@link org.springframework.data.couchbase.core.ReactiveFindByQueryOperation.FindByQueryWithProjection#as(Class) method}.
         */
        Assertions.assertFalse(foosView.isEmpty());
    }

//1.) Criteria and dynamic where clause formulation based on request body params:-
//        jsonRequest - HashMap having optional params coming in rest request body.
//        query - adding params at run time in Criteria.Which then sent to db template.
//        Document.class - generic bson document which return the resultset.Not bind to particular POJOs.
//                exclude() - excluding fields not request from result set.

    @Test
    public void testJsonRequest() {
        Map<String, Object> jsonRequest = new HashMap<String, Object>();
        jsonRequest.put("firstname", "Bar");
        jsonRequest.put("lastname", "Foo");
        jsonRequest.put("amount", 100);
        Query query = new Query();

        for (String key : jsonRequest.keySet()) {
            Object keyValue = jsonRequest.get(key);
            query.addCriteria(QueryCriteria.where(key).is(keyValue));
        }

        // With Couchbase the Select clause is not accessible through spring like it would be with Mongo.
        // It's inferred from the Entity POJO that was saved with the repository, and is identified with the _class field
        // As such the filter will always be made on that field, the Select clause built with the return type specified
        // in the as() method. You sadly cannot exclude field from the select query. We invite you to use SQL directly.


        List<CouchbaseDocument> foos = couchbaseTemplate.findByQuery(Foo.class).as(CouchbaseDocument.class).matching(query).all();
        System.out.println(foos);
    }

    //        2.) EventListener which provides methods like onAfterConvert(), onbeforeConvert() for managing the how DB attributes mapped with POJOs after result fetched.
//        Like in mongo - AbstractMongoEventListener
    @Test
    void testListener() {
        flushBucket();
        fooRepository.save(new Foo("Foo", "Bar", 101, sizes)).block();
        Assertions.assertTrue(CustomListener.counter.get() > 0);
    }

    //        3.) Regex operator support: -
//        Criteria.where(globalConstant).regex(request.replace(GlobalConstant.PERCENTAGE, "").trim())
//    - Regex are managed by the go standard library, more info on https://golang.org/pkg/regexp/syntax
    @Test
    public void testRegexCriteria() {
        Stream<Foo> document_list = Stream.of(new Foo("Foo", "Bar", 0, sizes), new Foo("Bor", "Bar", 0, sizes), new Foo("For", "Bar", 0, sizes));
        createFoos(document_list);
        Query q = new Query().addCriteria(QueryCriteria.where("firstname").regex(".o."));
        List<FooView> foos = couchbaseTemplate.findByQuery(Foo.class).as(FooView.class).matching(q).all();

        Assertions.assertEquals(3, foos.size());

        q = new Query().addCriteria(QueryCriteria.where("firstname").regex("F.*"));
        foos = couchbaseTemplate.findByQuery(Foo.class).as(FooView.class).matching(q).all();

        Assertions.assertEquals(2, foos.size());
    }

    //        4.) Logical operator support on Criteria: -
//        Criteria.where("something").gte(Double.valueOf("something"))
//        Here greater than equal to used to compare amount.
    @Test
    void testLogicalOperator() {
        createFoos(document_list);
        var criteria = QueryCriteria.where("amount").gte(0);
        Query q = new Query().addCriteria(criteria);
        List<Foo> foos = couchbaseTemplate.findByQuery(Foo.class).matching(q).all();
        Assertions.assertFalse(foos.isEmpty());
    }

    //        5.) Creating dynamic index
//        Based on search keys configured per type of record, creating dynamic index to facilitate faster bulk ingestion.
//        6.) Dropping index once ingestion completed: -
    @Test
    void createDynamicIndex() throws Exception {
        Collection col = couchbaseTemplate.getCollection("foo");
        CollectionQueryIndexManager indexManager = col.queryIndexes();
        String indexName = "MyIndex";

        int indexCreationRecordLimit = 1;

        List<String> search_keys_for_index_list = null;
        Map<String, Object> search_key_value_map_for_index = new HashMap<String, Object>();
        search_key_value_map_for_index.put("firstname", "Foo");
        int total_records_count = 99999;

        if (search_key_value_map_for_index != null && total_records_count > indexCreationRecordLimit) {
            Set<String> search_keys_for_index_set = search_key_value_map_for_index.keySet();
            search_keys_for_index_list = new ArrayList<>(search_keys_for_index_set);
            //This is added as prefix to create index
            search_keys_for_index_list.add("DELETE_ME");
            search_keys_for_index_list.sort(String::compareToIgnoreCase);
            indexManager.createIndex(indexName, search_keys_for_index_list);
            // Block until index creation
            indexManager.watchIndexes(List.of(indexName), Duration.ofSeconds(5));
        }
        // Run import
        Thread.sleep(1000);
        // Delete Collection Index
        indexManager.dropIndex(indexName);
    }

//        6.) Bulk Ingestion
    @Test
    void bulkOnBatchSize() {
        flushBucket();
        document_list.forEach(doc -> couchbaseTemplate.save(doc));
        Flux.range(1, 10000).map(i -> new Foo("Foo" + i, "Bar" + i, i, sizes))
                .parallel(10)
                .flatMap(doc -> couchbaseTemplate.reactive().save(doc)
                        .retryWhen(Retry.anyOf(CompletionException.class)
                                .exponentialBackoff(Duration.ofMillis(10), Duration.ofSeconds(1))
                                .doOnRetry(System.out::println)
                                .toReactorRetry())
                ).sequential()
                .blockLast(Duration.ofSeconds(10));
    }

    //        7.) Txn initiate
    // Be careful if you are testing on a one node cluster, you need to setup durability level accordingly.
//    https://docs.spring.io/spring-data/couchbase/docs/current/reference/html/#couchbase.transactions
    @Test
    void testTranscation() {
        String id = fooRepository.save(new Foo("Foo", "Bar", 0, sizes)).block().id;
        System.out.println(id);
        Flux<Foo> foo = txOperator.execute(ctx ->
                couchbaseTemplate.reactive().findById(Foo.class).one(id)
                        .flatMap(f -> {
                            f.setFirstname("Walt");
                            return couchbaseTemplate.reactive()
                                    .replaceById(Foo.class)
                                    .one(f);
                        })

        );
        Assertions.assertEquals("Walt", foo.blockLast().firstname);

        TransactionResult result = couchbaseTemplate.getCouchbaseClientFactory()
                .getCluster().transactions().run(ctx -> {
                    TransactionGetResult r = ctx.get(couchbaseTemplate.getCollection("foo"), id);
                    Foo f = r.contentAs(Foo.class);
                    f.setFirstname("Walter");
                    ctx.replace(r, f);
                });
        Assertions.assertTrue(result.unstagingComplete());
    }

    //        8.) doing rollback in case of exception
    @Test
    void testRollback() {
        String id = fooRepository.save(new Foo("Foo", "Bar", 0, sizes)).block().id;
        try {
            TransactionResult result = couchbaseTemplate.getCouchbaseClientFactory()
                    .getCluster().transactions().run(ctx -> {
                        TransactionGetResult r = ctx.get(couchbaseTemplate.getCollection("foo"), id);
                        Foo f = r.contentAs(Foo.class);
                        f.setFirstname("Walt");
                        ctx.replace(r, f);
                        // something is wrong
                        throw new IllegalArgumentException("Something is wrong");
                    });
        } catch (TransactionCommitAmbiguousException e) {
            //oh no !
        } catch (TransactionFailedException e) {
            //oh no !
        }
        Assertions.assertNotEquals("Walt", fooRepository.findById(id).block().firstname);

        try {
            Flux<Foo> foo = txOperator.execute(ctx ->
                    couchbaseTemplate.reactive().findById(Foo.class).one(id)
                            .map(f -> {
                                f.setFirstname("Walt");
                                return f;
                            })
                            .flatMap(p -> couchbaseTemplate.reactive()
                                    .replaceById(Foo.class)
                                    .one(p))
                            .flatMap(z -> {
                                ctx.setRollbackOnly();
                                return Mono.empty();
                            })
            );
        } catch (TransactionRollbackRequestedException r) {
            //oh no !
        }
        Assertions.assertNotEquals("Walt", fooRepository.findById(id).block().firstname);

    }

    //      9.) unwind or similar operator that allow to peel off a document for each element and returns that resulting document.
//        so if a doc has sub-array of 3 element then unwind will return 3 doc .
    @Test
    public void testUnwind() {
        // You need to use unnest, simple query would look like:
        createFoos(document_list);
        String queryStatement = "Select meta(`foo`).id as id, sizes from `foo` unnest sizes";
        List<JsonObject> foos = couchbaseTemplate.getCouchbaseClientFactory()
                .getBucket()
                .defaultScope()
                .query(queryStatement, QueryOptions.queryOptions().scanConsistency(QueryScanConsistency.REQUEST_PLUS))
                .rowsAsObject();
        Assertions.assertEquals(12, foos.size());
    }

    //        10.) % operator handling:
    @Test
    public void testLike() {
        Stream<Foo> document_list = Stream.of(
                new Foo("Foo", "Bar", 0, sizes),
                new Foo("Bor", "Bar", 0, sizes),
                new Foo("For", "Bar", 0, sizes));
        createFoos(document_list);
        Query q = new Query().addCriteria(QueryCriteria.where("firstname").like("Fo%"));
        List<FooView> foos = couchbaseTemplate.findByQuery(Foo.class).as(FooView.class).matching(q).all();
        Assertions.assertEquals(2, foos.size());
    }

}

