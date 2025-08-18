package com.example.springbootcomtrade.pojo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nan chao
 * @date 2024-10-22 15:45
 */

@Data
public class SampleData {

    private final int n;
    private final long timestampUs;
    private final List<Double> analogValues = new ArrayList<Double>();
    private final List<Double> digitalValues = new ArrayList<Double>();

    public SampleData(int n, long timestampUs) {
        this.n = n;
        this.timestampUs = timestampUs;
    }

    public void addAnalogValue(double value) {
        analogValues.add(value);
    }

    public void addDigitalValue(double value) {
        digitalValues.add(value);
    }

    public int getN() {
        return n;
    }

    public long getTimestampUs() {
        return timestampUs;
    }

    public List<Double> getAnalogValues() {
        return analogValues;
    }

    public List<Double> getDigitalValues() {
        return digitalValues;
    }
}
