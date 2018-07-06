package com.neo4jStudy.dataflow.entities.nodes;

import jdk.nashorn.internal.objects.annotations.Property;

import java.util.Objects;

/**
 * @author xyf
 * @Data Created in 15:40 2018/6/11
 * @Descriptions
 */
public class PersonNode {

    @Property(name="name")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersonNode)) return false;
        PersonNode person = (PersonNode) o;
        return Objects.equals(name, person.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name);
    }
}
