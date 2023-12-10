package de.htwberlin.dao;

import java.sql.Date;

public class Tray {
    private Integer trayID;
    private Integer diameterInCm;
    private Integer capacity;
    private Date expirationDate;
    private Boolean setExpirationDateManually = false;

    public Integer getTrayID() {
        return trayID;
    }

    public void setTrayID(Integer trayID) {
        this.trayID = trayID;
    }

    public Integer getDiameterInCm() {
        return diameterInCm;
    }
    public Boolean getSetExpirationDateManually() { return setExpirationDateManually; }

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
    public void setSetExpirationDateManually(Boolean setExpirationDateManually) { this.setExpirationDateManually = setExpirationDateManually; }
}
