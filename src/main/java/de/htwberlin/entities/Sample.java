package de.htwberlin.entities;

import java.time.format.DateTimeFormatter;
import java.sql.Date;

public class Sample {
    private Integer sampleID;
    private Date expirationDate;
    private String sampleName;
    private Integer validNumberOfDays;

    public Integer getSampleID() {
        return sampleID;
    }

    public String getSampleName() {
        return sampleName;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public Integer getValidNumberOfDays() {
        return validNumberOfDays;
    }

    public void setValidNumberOfDays(Integer validNumberOfDays) {
        this.validNumberOfDays = validNumberOfDays;
    }

    public void setSampleID(Integer sampleID) {
        this.sampleID = sampleID;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }
}
