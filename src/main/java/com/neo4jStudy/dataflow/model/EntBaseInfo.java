package com.neo4jStudy.dataflow.model;

import java.util.Objects;

/**
 * @author xyf
 * @Data Created in 17:27 2018/6/11
 * @Descriptions
 */
public class EntBaseInfo {
    private String PRIPID;
    private String ENTNAME;
    private String CREDIT_CODE;
    private String REGNO;
    private String REGCAP;
    private String REGCAPCUR;
    private String ENTSTATUS;
    private String ESDATE;
    private String INDUSTRYPHY;
    private String NAME;
    private String ZSPID;

    public String getPRIPID() {
        return PRIPID;
    }

    public void setPRIPID(String PRIPID) {
        this.PRIPID = PRIPID;
    }

    public String getENTNAME() {
        return ENTNAME;
    }

    public void setENTNAME(String ENTNAME) {
        this.ENTNAME = ENTNAME;
    }

    public String getCREDIT_CODE() {
        return CREDIT_CODE;
    }

    public void setCREDIT_CODE(String CREDIT_CODE) {
        this.CREDIT_CODE = CREDIT_CODE;
    }

    public String getREGNO() {
        return REGNO;
    }

    public void setREGNO(String REGNO) {
        this.REGNO = REGNO;
    }

    public String getREGCAP() {
        return REGCAP;
    }

    public void setREGCAP(String REGCAP) {
        this.REGCAP = REGCAP;
    }

    public String getREGCAPCUR() {
        return REGCAPCUR;
    }

    public void setREGCAPCUR(String REGCAPCUR) {
        this.REGCAPCUR = REGCAPCUR;
    }

    public String getENTSTATUS() {
        return ENTSTATUS;
    }

    public void setENTSTATUS(String ENTSTATUS) {
        this.ENTSTATUS = ENTSTATUS;
    }

    public String getESDATE() {
        return ESDATE;
    }

    public void setESDATE(String ESDATE) {
        this.ESDATE = ESDATE;
    }

    public String getINDUSTRYPHY() {
        return INDUSTRYPHY;
    }

    public void setINDUSTRYPHY(String INDUSTRYPHY) {
        this.INDUSTRYPHY = INDUSTRYPHY;
    }

    public String getNAME() {
        return NAME;
    }

    public void setNAME(String NAME) {
        this.NAME = NAME;
    }

    public String getZSPID() {
        return ZSPID;
    }

    public void setZSPID(String ZSPID) {
        this.ZSPID = ZSPID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntBaseInfo)) return false;
        EntBaseInfo that = (EntBaseInfo) o;
        return Objects.equals(PRIPID, that.PRIPID) &&
                Objects.equals(ENTNAME, that.ENTNAME) &&
                Objects.equals(CREDIT_CODE, that.CREDIT_CODE) &&
                Objects.equals(REGNO, that.REGNO) &&
                Objects.equals(REGCAP, that.REGCAP) &&
                Objects.equals(REGCAPCUR, that.REGCAPCUR) &&
                Objects.equals(ENTSTATUS, that.ENTSTATUS) &&
                Objects.equals(ESDATE, that.ESDATE) &&
                Objects.equals(INDUSTRYPHY, that.INDUSTRYPHY) &&
                Objects.equals(NAME, that.NAME) &&
                Objects.equals(ZSPID, that.ZSPID);
    }

    @Override
    public int hashCode() {

        return Objects.hash(PRIPID, ENTNAME, CREDIT_CODE, REGNO, REGCAP, REGCAPCUR, ENTSTATUS, ESDATE, INDUSTRYPHY, NAME, ZSPID);
    }
}
