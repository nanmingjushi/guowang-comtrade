package com.example.springbootcomtrade.pojo;

import lombok.Data;

/**
 * @author nan chao
 * @date 2024-10-22 10:05
 */
@Data
public class AnalogChannel {
    private String an;
    private String chId;
    private String ph;
    private String ccbm;
    private String uu;
    private double a;
    private double b;
    private double skew;
    private String min;
    private String max;
    private double primary;
    private double secondary;
    private String ps;


    // Default constructor
    public AnalogChannel() {}

    // Parameterized constructor
    public AnalogChannel(String an, String chId, String ph, String ccbm, String uu, double a, double b, double skew, String min, String max, double primary, double secondary, String ps) {
        this.an = an;
        this.chId = chId;
        this.ph = ph;
        this.ccbm = ccbm;
        this.uu = uu;
        this.a = a;
        this.b = b;
        this.skew = skew;
        this.min = min;
        this.max = max;
        this.primary = primary;
        this.secondary = secondary;
        this.ps = ps;
    }

    // Getters and Setters

    public String getAn() {
        return an;
    }

    public void setAn(String an) {
        this.an = an;
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

    public String getUu() {
        return uu;
    }

    public void setUu(String uu) {
        this.uu = uu;
    }

    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public double getSkew() {
        return skew;
    }

    public void setSkew(double skew) {
        this.skew = skew;
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public double getPrimary() {
        return primary;
    }

    public void setPrimary(double primary) {
        this.primary = primary;
    }

    public double getSecondary() {
        return secondary;
    }

    public void setSecondary(double secondary) {
        this.secondary = secondary;
    }

    public String getPs() {
        return ps;
    }

    public void setPs(String ps) {
        this.ps = ps;
    }
}
