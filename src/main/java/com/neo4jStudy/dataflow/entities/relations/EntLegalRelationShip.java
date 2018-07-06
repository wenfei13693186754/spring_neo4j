package com.neo4jStudy.dataflow.entities.relations;

import com.neo4jStudy.dataflow.common.Constants;
import com.neo4jStudy.dataflow.entities.BaseEntity;
import com.neo4jStudy.dataflow.entities.nodes.EntNode;
import com.neo4jStudy.dataflow.entities.nodes.PersonNode;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import java.util.Objects;

/**
 * @author xyf
 * @Data Created in 16:28 2018/6/11
 * @Descriptions    法人关系
 */
@RelationshipEntity(type = Constants.LEGAL)
public class EntLegalRelationShip extends BaseEntity {

    @StartNode
    private PersonNode person;

    @EndNode
    private EntNode ent;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntLegalRelationShip)) return false;
        if (!super.equals(o)) return false;
        EntLegalRelationShip that = (EntLegalRelationShip) o;
        return Objects.equals(person, that.person) &&
                Objects.equals(ent, that.ent);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), person, ent);
    }
}
