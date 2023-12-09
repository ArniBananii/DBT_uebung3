package de.htwberlin.dao;

public class Place {
    private Integer sampleID;
    private Integer trayID;
    private Integer planceNo;

    public Integer getPlaceID() {
        return sampleID;
    }

    public void setSampleID(Integer placeID) {
        this.sampleID = placeID;
    }

    public Integer getTrayID() {
        return trayID;
    }

    public void setTrayID(Integer trayID) {
        this.trayID = trayID;
    }

    public Integer getPlanceNo() {
        return planceNo;
    }

    public void setPlanceNo(Integer planceNo) {
        this.planceNo = planceNo;
    }
}
