package com.couchbase;

import org.springframework.data.couchbase.repository.ReactiveCouchbaseRepository;

public interface FooRepository extends ReactiveCouchbaseRepository<Foo, String> {
}
