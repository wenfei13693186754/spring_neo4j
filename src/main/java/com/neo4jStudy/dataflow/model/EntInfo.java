package com.neo4jStudy.dataflow.model;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @author xyf
 * @Data Created in 17:25 2018/6/11
 * @Descriptions
 */
public class EntInfo {
    private EntBaseInfo entBaseInfo;
    private ArrayList<InvInfo> invInfos;
    private ArrayList<StaffInfo> staffInfos;

    public EntBaseInfo getEntBaseInfo() {
        return entBaseInfo;
    }

    public void setEntBaseInfo(EntBaseInfo entBaseInfo) {
        this.entBaseInfo = entBaseInfo;
    }

    public ArrayList<InvInfo> getInvInfos() {
        return invInfos;
    }

    public void setInvInfos(ArrayList<InvInfo> invInfos) {
        this.invInfos = invInfos;
    }

    public ArrayList<StaffInfo> getStaffInfos() {
        return staffInfos;
    }

    public void setStaffInfos(ArrayList<StaffInfo> staffInfos) {
        this.staffInfos = staffInfos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntInfo)) return false;
        EntInfo entInfo = (EntInfo) o;
        return Objects.equals(entBaseInfo, entInfo.entBaseInfo) &&
                Objects.equals(invInfos, entInfo.invInfos) &&
                Objects.equals(staffInfos, entInfo.staffInfos);
    }

    @Override
    public int hashCode() {

        return Objects.hash(entBaseInfo, invInfos, staffInfos);
    }
}
