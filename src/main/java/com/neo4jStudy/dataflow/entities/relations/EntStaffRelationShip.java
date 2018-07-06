package com.neo4jStudy.dataflow.entities.relations;

import com.neo4jStudy.dataflow.common.Constants;
import com.neo4jStudy.dataflow.entities.BaseEntity;
import com.neo4jStudy.dataflow.entities.nodes.EntNode;
import com.neo4jStudy.dataflow.entities.nodes.PersonNode;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import java.util.Objects;

/**
 * @author xyf
 * @Data Created in 15:36 2018/6/11
 * @Descriptions    企业高管关系
 */
@RelationshipEntity(type = Constants.STAFF)
public class EntStaffRelationShip extends BaseEntity {

    @StartNode
    private PersonNode person;

    @EndNode
    private EntNode ent;

    @Property(name = "position")
    private String position;

    public PersonNode getPerson() {
        return person;
    }

    public void setPerson(PersonNode person) {
        this.person = person;
    }

    public EntNode getEnt() {
        return ent;
    }

    public void setEnt(EntNode ent) {
        this.ent = ent;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntStaffRelationShip)) return false;
        if (!super.equals(o)) return false;
        EntStaffRelationShip that = (EntStaffRelationShip) o;
        return Objects.equals(person, that.person) &&
                Objects.equals(ent, that.ent) &&
                Objects.equals(position, that.position);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), person, ent, position);
    }
}
