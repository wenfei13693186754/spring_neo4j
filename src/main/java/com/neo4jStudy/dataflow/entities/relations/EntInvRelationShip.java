package com.neo4jStudy.dataflow.entities.relations;

import com.neo4jStudy.dataflow.common.Constants;
import com.neo4jStudy.dataflow.entities.nodes.EntNode;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

/**
 * @author xyf
 * @Data Created in 17:03 2018/6/11
 * @Descriptions
 */
@RelationshipEntity(type = Constants.INV)
public class EntInvRelationShip {
    @StartNode
    private EntNode fromEnt;

    @EndNode
    private EntNode toEnt;

    @Property(name = "condate")
    private String condate;

    @Property(name = "conprop")
    private String conprop;

    @Property(name = "currency")
    private String currency;

    @Property(name = "subconam")
    private String subconam;


    private String holderamt;
    private String holderrto;

    public EntNode getFromEnt() {
        return fromEnt;
    }

    public void setFromEnt(EntNode fromEnt) {
        this.fromEnt = fromEnt;
    }

    public EntNode getToEnt() {
        return toEnt;
    }

    public void setToEnt(EntNode toEnt) {
        this.toEnt = toEnt;
    }

    public String getConprop() {
        return conprop;
    }

    public void setConprop(String conprop) {
        this.conprop = conprop;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getSubconam() {
        return subconam;
    }

    public void setSubconam(String subconam) {
        this.subconam = subconam;
    }

    public String getCondate() {
        return condate;
    }

    public void setCondate(String condate) {
        this.condate = condate;
    }

    public String getHolderamt() {
        return holderamt;
    }

    public void setHolderamt(String holderamt) {
        this.holderamt = holderamt;
    }

    public String getHolderrto() {
        return holderrto;
    }

    public void setHolderrto(String holderrto) {
        this.holderrto = holderrto;
    }
}
