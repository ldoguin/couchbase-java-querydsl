package com.couchbase;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.couchbase.core.mapping.id.GeneratedValue;
import org.springframework.data.couchbase.core.mapping.id.GenerationStrategy;
import org.springframework.data.couchbase.repository.Collection;

import java.util.List;

@Collection("foo")
public class Foo {

    @Id
    @GeneratedValue(strategy = GenerationStrategy.UNIQUE)
    public String id;

    public String firstname;

    public String lastname;

    public float amount;

    public List<String> sizes;

    @Version
    private Long cas; // Necessary for transaction management with Spring

    public Foo() {
    }

    public Foo(String firstname, String lastname, float amount, List<String> sizes) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.amount = amount;
        this.sizes = sizes;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    @Override
    public String toString() {
        return "Foo{" +
                "id='" + id + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", amount='" + amount + '\'' +
                ", sizes='" + sizes + '\'' +
                '}';
    }
}
