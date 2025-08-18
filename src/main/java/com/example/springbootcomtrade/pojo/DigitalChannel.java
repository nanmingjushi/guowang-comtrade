package com.example.springbootcomtrade.pojo;

import lombok.Data;

/**
 * @author nan chao
 * @date 2024-10-22 10:07
 */

@Data
public class DigitalChannel {
    private String dn;
    private String chId;
    private String ph;
    private String ccbm;
    private String y;

    // Getters and Setters

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public String getChId() {
        return chId;
    }

    public void setChId(String chId) {
        this.chId = chId;
    }

    public String getPh() {
        return ph;
    }

    public void setPh(String ph) {
        this.ph = ph;
    }

    public String getCcbm() {
        return ccbm;
    }

    public void setCcbm(String ccbm) {
        this.ccbm = ccbm;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }
}
