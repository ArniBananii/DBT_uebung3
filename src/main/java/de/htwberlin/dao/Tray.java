package de.htwberlin.dao;


import java.sql.Date;

public class Tray {
    private Integer trayID;
    private Integer diameterInCm;
    private Integer capacity;
    private Date expirationDate;

    public Integer getTrayID() {
        return trayID;
    }

    public void setTrayID(Integer trayID) {
        this.trayID = trayID;
    }

    public Integer getDiameterInCm() {
        return diameterInCm;
    }

    public void setDiameterInCm(Integer diameterInCm) {
        this.diameterInCm = diameterInCm;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }
}