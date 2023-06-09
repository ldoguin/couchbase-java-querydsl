package com.couchbase;

import com.couchbase.client.java.query.Statement;
import com.couchbase.client.java.query.dsl.Expression;
import com.couchbase.client.java.query.dsl.path.DefaultSelectPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.data.couchbase.core.mapping.id.GeneratedValue;
import org.springframework.data.couchbase.core.mapping.id.GenerationStrategy;
import org.springframework.data.couchbase.core.query.Query;
import org.springframework.data.couchbase.core.query.QueryCriteria;
import org.springframework.data.couchbase.repository.CouchbaseRepository;

import java.util.List;

import static com.couchbase.client.java.query.dsl.Expression.s;
import static com.couchbase.client.java.query.dsl.Expression.x;

@SpringBootTest()
class QueryDSLTest {

    @Autowired
    FooRepository fooRepository;

    @Autowired
    CouchbaseTemplate couchbaseTemplate;

    @Test
    void testQueryDSL() {
        createFoos();

        Statement statement = new DefaultSelectPath(null)
                .select(Expression.x("firstname"), x("lastname"))
                .from("default")
                .where(x("lastname").eq(s("Foo")))
                .exceptAll()
                .select(x("firstname"), x("lastname"))
                .from("default")
                .where(x("lastname").eq(s("Bar")));

        List<Foo> foos = fooRepository.getOperations().getCouchbaseClientFactory().getCluster()
                .query(statement.toString()).rowsAs(Foo.class);
        System.out.println(foos);

        List<FooView> foosView = fooRepository.getOperations().getCouchbaseClientFactory().getCluster()
                .query(statement.toString()).rowsAs(FooView.class);

        System.out.println(foosView);


    }

    @Test
    void testSpringQueryCriteria() {
        createFoos();

        Query q = new Query()
                .addCriteria(QueryCriteria.where("firstname").eq("Foo"));
        List<Foo> allFoo = couchbaseTemplate.findByQuery(Foo.class).matching(q).all();
        System.out.println(allFoo);
        List<FooView> allFooView = couchbaseTemplate.findByQuery(FooView.class).matching(q).all();
        System.out.println(allFooView);
    }

    public void createFoos(){
        fooRepository.save(new Foo("Foo","Bar"));
        fooRepository.save(new Foo("Bar","Bar"));
        fooRepository.save(new Foo("Foo","Foo"));
        fooRepository.save(new Foo("Bar","Foo"));
    }
}

class Foo {

    @Id
    @GeneratedValue(strategy = GenerationStrategy.UNIQUE)
    public String id;

    public String firstname;

    public String lastname;

    public Foo() {
    }

    public Foo(String firstname, String lastname) {
        this.firstname = firstname;
        this.lastname = lastname;
    }

    @Override
    public String toString() {
        return "Foo{" +
                "id='" + id + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                '}';
    }
}

class FooView {

    public FooView() {
    }

    public String firstname;

    @Override
    public String toString() {
        return "FooView{" +
                "firstname='" + firstname + '\'' +
                '}';
    }
}
interface FooRepository extends CouchbaseRepository<Foo, String>{}