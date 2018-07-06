package com.neo4jStudy.dataflow.entities.nodes;

import com.neo4jStudy.dataflow.entities.BaseEntity;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

import java.util.Objects;

/**
 * @author xyf
 * @Data Created in 16:11 2018/6/11
 * @Descriptions    企业节点信息Bean
 */
@NodeEntity(label = "ent")
public class EntNode extends BaseEntity {

    @GraphId
    private Long id;

    @Property(name = "esdate")
    private String esdate;

    @Property(name = "regno")
    private String regno;

    @Property(name = "creditcode")
    private String creditcode;

    @Property(name = "regcap")
    private String regcap;

    @Property(name = "name")
    private String name;

    @Property(name = "entstatus")
    private String entstatus;

    @Property(name = "industryphy")
    private String industryphy;

    @Property(name = "zsid")
    private String zsid;

    @Property(name = "regcapcur")
    private String regcapcur;

    @Property(name = "type")
    private String type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEsdate() {
        return esdate;
    }

    public void setEsdate(String esdate) {
        this.esdate = esdate;
    }

    public String getRegno() {
        return regno;
    }

    public void setRegno(String regno) {
        this.regno = regno;
    }

    public String getCreditcode() {
        return creditcode;
    }

    public void setCreditcode(String creditcode) {
        this.creditcode = creditcode;
    }

    public String getRegcap() {
        return regcap;
    }

    public void setRegcap(String regcap) {
        this.regcap = regcap;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEntstatus() {
        return entstatus;
    }

    public void setEntstatus(String entstatus) {
        this.entstatus = entstatus;
    }

    public String getIndustryphy() {
        return industryphy;
    }

    public void setIndustryphy(String industryphy) {
        this.industryphy = industryphy;
    }

    public String getZsid() {
        return zsid;
    }

    public void setZsid(String zsid) {
        this.zsid = zsid;
    }

    public String getRegcapcur() {
        return regcapcur;
    }

    public void setRegcapcur(String regcapcur) {
        this.regcapcur = regcapcur;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntNode)) return false;
        EntNode ent = (EntNode) o;
        return  Objects.equals(esdate, ent.esdate) &&
                Objects.equals(regno, ent.regno) &&
                Objects.equals(creditcode, ent.creditcode) &&
                Objects.equals(regcap, ent.regcap) &&
                Objects.equals(name, ent.name) &&
                Objects.equals(entstatus, ent.entstatus) &&
                Objects.equals(industryphy, ent.industryphy) &&
                Objects.equals(zsid, ent.zsid) &&
                Objects.equals(regcapcur, ent.regcapcur);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, esdate, regno, creditcode, regcap, name, entstatus, industryphy, zsid, regcapcur);
    }
}
