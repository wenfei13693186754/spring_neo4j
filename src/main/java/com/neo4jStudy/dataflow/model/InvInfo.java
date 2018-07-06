package com.neo4jStudy.dataflow.model;

import java.util.Objects;

/**
 * @author xyf
 * @Data Created in 17:28 2018/6/11
 * @Descriptions
 */
public class InvInfo {
    private String PRIPID;
    private String INVID;
    private String INVTYPE;
    private String ZSPID;
    private String INV;
    private String CONDATE;
    private String CURRENCY;
    private String SUBCONAM;
    private String CONPROP;
    private String HOLDERAMT;
    private String HOLDERRTO;
    private String SHARESTYPE;

    public String getPRIPID() {
        return PRIPID;
    }

    public void setPRIPID(String PRIPID) {
        this.PRIPID = PRIPID;
    }

    public String getINVID() {
        return INVID;
    }

    public void setINVID(String INVID) {
        this.INVID = INVID;
    }

    public String getINVTYPE() {
        return INVTYPE;
    }

    public void setINVTYPE(String INVTYPE) {
        this.INVTYPE = INVTYPE;
    }

    public String getZSPID() {
        return ZSPID;
    }

    public void setZSPID(String ZSPID) {
        this.ZSPID = ZSPID;
    }

    public String getINV() {
        return INV;
    }

    public void setINV(String INV) {
        this.INV = INV;
    }

    public String getCONDATE() {
        return CONDATE;
    }

    public void setCONDATE(String CONDATE) {
        this.CONDATE = CONDATE;
    }

    public String getCURRENCY() {
        return CURRENCY;
    }

    public void setCURRENCY(String CURRENCY) {
        this.CURRENCY = CURRENCY;
    }

    public String getSUBCONAM() {
        return SUBCONAM;
    }

    public void setSUBCONAM(String SUBCONAM) {
        this.SUBCONAM = SUBCONAM;
    }

    public String getCONPROP() {
        return CONPROP;
    }

    public void setCONPROP(String CONPROP) {
        this.CONPROP = CONPROP;
    }

    public String getHOLDERAMT() {
        return HOLDERAMT;
    }

    public void setHOLDERAMT(String HOLDERAMT) {
        this.HOLDERAMT = HOLDERAMT;
    }

    public String getHOLDERRTO() {
        return HOLDERRTO;
    }

    public void setHOLDERRTO(String HOLDERRTO) {
        this.HOLDERRTO = HOLDERRTO;
    }

    public String getSHARESTYPE() {
        return SHARESTYPE;
    }

    public void setSHARESTYPE(String SHARESTYPE) {
        this.SHARESTYPE = SHARESTYPE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InvInfo)) return false;
        InvInfo invInfo = (InvInfo) o;
        return Objects.equals(PRIPID, invInfo.PRIPID) &&
                Objects.equals(INVID, invInfo.INVID) &&
                Objects.equals(INVTYPE, invInfo.INVTYPE) &&
                Objects.equals(ZSPID, invInfo.ZSPID) &&
                Objects.equals(INV, invInfo.INV) &&
                Objects.equals(CONDATE, invInfo.CONDATE) &&
                Objects.equals(CURRENCY, invInfo.CURRENCY) &&
                Objects.equals(SUBCONAM, invInfo.SUBCONAM) &&
                Objects.equals(CONPROP, invInfo.CONPROP) &&
                Objects.equals(HOLDERAMT, invInfo.HOLDERAMT) &&
                Objects.equals(HOLDERRTO, invInfo.HOLDERRTO) &&
                Objects.equals(SHARESTYPE, invInfo.SHARESTYPE);
    }

    @Override
    public int hashCode() {

        return Objects.hash(PRIPID, INVID, INVTYPE, ZSPID, INV, CONDATE, CURRENCY, SUBCONAM, CONPROP, HOLDERAMT, HOLDERRTO, SHARESTYPE);
    }
}
