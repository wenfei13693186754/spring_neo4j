package com.neo4jStudy.dataflow.model;

import com.neo4jStudy.dataflow.entities.nodes.EntNode;
import com.neo4jStudy.dataflow.entities.nodes.PersonNode;

import java.util.Map;

/**
 * @author xyf
 * @Data Created in 13:55 2018/6/5
 * @Descriptions
 */
public class EntAssociaNodeInfo {
    private EntNode entNode;//企业

    private PersonNode legalNode;//法人

    private Map<String, PersonNode> staffs;//投资人

    private Map<String, PersonNode> teninvmerge;//高管

    public EntNode getEntNode() {
        return entNode;
    }

    public void setEntNode(EntNode entNode) {
        this.entNode = entNode;
    }

    public PersonNode getLegalNode() {
        return legalNode;
    }

    public void setLegalNode(PersonNode legalNode) {
        this.legalNode = legalNode;
    }

    public Map<String, PersonNode> getStaffs() {
        return staffs;
    }

    public void setStaffs(Map<String, PersonNode> staffs) {
        this.staffs = staffs;
    }

    public Map<String, PersonNode> getTeninvmerge() {
        return teninvmerge;
    }

    public void setTeninvmerge(Map<String, PersonNode> teninvmerge) {
        this.teninvmerge = teninvmerge;
    }
}
