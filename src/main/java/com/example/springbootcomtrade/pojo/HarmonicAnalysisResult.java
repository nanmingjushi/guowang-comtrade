package com.example.springbootcomtrade.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// 用于存储谐波分析结果的POJO类
public class HarmonicAnalysisResult {

    // 获取谐波电压含有率列表
    // 用于存储每次分析得到的谐波电压含有率列表
    @Getter
    @Setter
    private List<List<Double>> HRUh;

    @Getter
    @Setter
    // 用于存储每次分析得到的总谐波畸变率（THD）列表
    private List<Double> THD;

    // 用于存储各次谐波电压含有率的95%值列表
    private List<Double> HRUh95Percentiles;

    // 用于存储总谐波畸变率（THD）的95%值
    private double THD95Percentile;


    public HarmonicAnalysisResult() {
        HRUh = new ArrayList<>();
        THD = new ArrayList<>();
        HRUh95Percentiles = new ArrayList<>();
    }
    // 获取各次谐波电压含有率的95%值列表
    public List<Double> getHRUh95Percentiles() {
        return HRUh95Percentiles;
    }

    // 设置各次谐波电压含有率的95%值列表
    public void setHRUh95Percentiles(List<Double> HRUh95Percentiles) {
        this.HRUh95Percentiles = HRUh95Percentiles;
    }

    // 获取总谐波畸变率（THD）的95%值
    public double getTHD95Percentile() {
        return THD95Percentile;
    }

    // 设置总谐波畸变率（THD）的95%值
    public void setTHD95Percentile(double THD95Percentile) {
        this.THD95Percentile = THD95Percentile;
    }
    // 将分析结果格式化为指定的字符串样式
    public String formatResult() {
        StringBuilder resultBuilder = new StringBuilder();

        resultBuilder.append("各次谐波电压含有率的95%值：\n");
        for (int i = 0; i < HRUh95Percentiles.size(); i++) {
            resultBuilder.append("第 ").append(i + 2).append(" 次谐波：").append(HRUh95Percentiles.get(i)).append("%\n");
        }

        resultBuilder.append("总谐波畸变率（THD）的95%值：").append(THD95Percentile).append("%\n");

        return resultBuilder.toString();
    }
}

