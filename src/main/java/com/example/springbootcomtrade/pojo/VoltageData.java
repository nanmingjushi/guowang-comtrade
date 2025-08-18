package com.example.springbootcomtrade.pojo;

import java.util.ArrayList;
import java.util.List;

// 用于存储从CSV文件读取的电压数据的POJO类
public class VoltageData {

    private List<Double> voltage;

    public VoltageData() {
        voltage = new ArrayList<>();
    }

    public List<Double> getVoltage() {
        return voltage;
    }

    public void setVoltage(List<Double> voltage) {
        this.voltage = voltage;
    }
}