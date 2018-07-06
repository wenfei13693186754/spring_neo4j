package com.neo4jStudy.dataflow.model;

import java.util.Objects;

/**
 * @author xyf
 * @Data Created in 17:31 2018/6/11
 * @Descriptions    高管信息
 */
public class StaffInfo {
    private String PRIPID;
    private String NAME;
    private String POSITION;
    private String ZSPID;

    public String getPRIPID() {
        return PRIPID;
    }

    public void setPRIPID(String PRIPID) {
        this.PRIPID = PRIPID;
    }

    public String getNAME() {
        return NAME;
    }

    public void setNAME(String NAME) {
        this.NAME = NAME;
    }

    public String getPOSITION() {
        return POSITION;
    }

    public void setPOSITION(String POSITION) {
        this.POSITION = POSITION;
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
        if (!(o instanceof StaffInfo)) return false;
        StaffInfo staffInfo = (StaffInfo) o;
        return Objects.equals(PRIPID, staffInfo.PRIPID) &&
                Objects.equals(NAME, staffInfo.NAME) &&
                Objects.equals(POSITION, staffInfo.POSITION) &&
                Objects.equals(ZSPID, staffInfo.ZSPID);
    }

    @Override
    public int hashCode() {

        return Objects.hash(PRIPID, NAME, POSITION, ZSPID);
    }
}
