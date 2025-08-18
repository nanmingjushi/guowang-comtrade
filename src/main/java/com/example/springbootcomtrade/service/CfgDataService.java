package com.example.springbootcomtrade.service;

import com.example.springbootcomtrade.pojo.AnalogChannel;
import com.example.springbootcomtrade.pojo.DigitalChannel;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class CfgDataService {

    private List<AnalogChannel> analogChannels; // 模拟通道列表
    private List<DigitalChannel> digitalChannels; // 数字通道列表
    private double timemult; // 时间标记倍乘系数

    // 声明CFG文件中各个参数变量
    String stationName = "";
    String recDev = "";
    String revYear = "";
    int channelsNum = 0;
    int analogNum = 0; // 模拟通道数
    int digitalNum = 0; // 数字通道数
    double lineFrequency = 0;
    int nrates = 0;
    List<Double> samplingFrequencies = new ArrayList<>();
    List<Integer> endsamps = new ArrayList<>();
    String startTimestamp_date = "";
    String startTimestamp_time="";
    String triggerTimestamp_date = "";
    String triggerTimestamp_time = "";
    String ft = "";

    // 用于存储最大采样频率及其对应的endsamp和前一个endsamp
    private double maxSampFrequency;
    private int maxEndsamp;
    private int maxPreviousEndsamp;

    // 读取已知路径的cfg文件，接收字符串路径，后端写
    public void readCfgFile(String cfgFilePath) throws IOException {
        analogChannels = new ArrayList<>();
        digitalChannels = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(cfgFilePath))) {
            String line;
            int lineNumber = 0;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (lineNumber == 0) {
                    // 解析厂站名称等
                    stationName = parts[0];
                    recDev = parts[1];
                    revYear = parts[2];
                    System.out.printf("厂站名称：%s, 记录标识：%s, 版本年号：%s%n", stationName, recDev, revYear);

                } else if (lineNumber == 1) {
                    // 解析通道数
                    channelsNum = Integer.parseInt(parts[0]);
                    analogNum = Integer.parseInt(parts[1].replace("A", ""));
                    digitalNum = Integer.parseInt(parts[2].replace("D", ""));
                    System.out.printf("通道总数：%d, 模拟通道数：%d, 数字通道数：%d%n", channelsNum, analogNum, digitalNum);

                } else if (lineNumber >= 2 && lineNumber < 2 + analogNum) {
                    // 解析模拟通道
                    AnalogChannel channel = new AnalogChannel();
                    channel.setAn(parts[0]);
                    channel.setChId(parts[1]);
                    channel.setPh(parts[2]);
                    channel.setCcbm(parts[3]);
                    channel.setUu(parts[4]);
                    channel.setA(Double.parseDouble(parts[5]));
                    channel.setB(Double.parseDouble(parts[6]));
                    channel.setSkew(Double.parseDouble(parts[7]));
                    channel.setMin(parts[8]);
                    channel.setMax(parts[9]);
                    channel.setPrimary(Double.parseDouble(parts[10]));
                    channel.setSecondary(Double.parseDouble(parts[11]));
                    channel.setPs(parts[12]);
                    analogChannels.add(channel);
                } else if (lineNumber >= 2 + analogNum && lineNumber < 2 + analogNum + digitalNum) {
                    // 解析数字通道
                    DigitalChannel channel = new DigitalChannel();
                    channel.setDn(parts[0]);
                    channel.setChId(parts[1]);
                    channel.setPh(parts[2]);
                    channel.setCcbm(parts[3]);
                    channel.setY(parts[4]);
                    digitalChannels.add(channel);
                } else if (lineNumber == 2 + analogNum + digitalNum) {
                    // 名义线路频率
                    lineFrequency = Double.parseDouble(parts[0]);
                    System.out.printf("线路频率: %.2f%n", lineFrequency);
                } else if (lineNumber == 3 + analogNum + digitalNum) {
                    // 采样频率个数
                    nrates = Integer.parseInt(parts[0]);
                    System.out.printf("采样频率个数: %d%n", nrates);
                } else if (lineNumber >= 4 + analogNum + digitalNum && lineNumber < 4 + analogNum + digitalNum + nrates) {
                    // 保存各个采样频率和最后采样
                    double sampFrequency = Double.parseDouble(parts[0]);
                    int endsamp = Integer.parseInt(parts[1]);
                    samplingFrequencies.add(sampFrequency);
                    endsamps.add(endsamp);
                    System.out.printf("采样频率: %.2f, 最后采样点: %d%n", sampFrequency, endsamp);
                } else if (lineNumber == 4 + analogNum + digitalNum + nrates) {
                    // 第一个采样点的日期和时间
                    startTimestamp_date = parts[0];
                    startTimestamp_time = parts[1];
                    System.out.println("起始时间戳: "+startTimestamp_date +" "+ startTimestamp_time);
                } else if (lineNumber == 5 + analogNum + digitalNum + nrates) {
                    // 触发点的日期和时间
                    triggerTimestamp_date = parts[0];
                    triggerTimestamp_time=parts[1];
                    System.out.println("触发时间戳: "+ triggerTimestamp_date+" "+triggerTimestamp_time);
                } else if (lineNumber == 6 + analogNum + digitalNum + nrates) {
                    // DAT数据文件格式
                    ft = parts[0];
                    System.out.printf("数据文件格式: %s%n", ft);
                } else if (lineNumber == 7 + analogNum + digitalNum + nrates) {
                    // 时间标记倍乘系数
                    timemult = Double.parseDouble(parts[0]);
                    System.out.printf("时间标记倍乘系数: %.2f%n", timemult);
                }

                lineNumber++;
                // 在读取完所有采样频率和endsamp后进行选取操作
                if (lineNumber == 4 + analogNum + digitalNum + nrates) {
                    selectMaxSamplingFrequencyAndEndsamps();
                }
            }
        }
    }


    // 从输入流读取而不是从文件路径读取。接收的是cfg文件，MultipartFile对象,前端上传
    public void readCfgFromMultipart(MultipartFile cfgFile) throws IOException {
        // 初始化模拟通道列表和数字通道列表
        analogChannels = new ArrayList<>();
        digitalChannels = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(cfgFile.getInputStream(), "GBK"))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine())!= null) {
                String[] parts = line.split(",");
                if (lineNumber == 0) {
                    // 解析厂站名称等
                    stationName = parts[0];
                    recDev = parts[1];
                    revYear = parts[2];
                    System.out.printf("厂站名称：%s, 记录标识：%s, 版本年号：%s%n", stationName, recDev, revYear);

                } else if (lineNumber == 1) {
                    // 解析通道数
                    channelsNum = Integer.parseInt(parts[0]);
                    analogNum = Integer.parseInt(parts[1].replace("A", ""));
                    digitalNum = Integer.parseInt(parts[2].replace("D", ""));
                    System.out.printf("通道总数：%d, 模拟通道数：%d, 数字通道数：%d%n", channelsNum, analogNum, digitalNum);

                } else if (lineNumber >= 2 && lineNumber < 2 + analogNum) {
                    // 解析模拟通道
                    AnalogChannel channel = new AnalogChannel();
                    channel.setAn(parts[0]);
                    channel.setChId(parts[1]);
                    channel.setPh(parts[2]);
                    channel.setCcbm(parts[3]);
                    channel.setUu(parts[4]);
                    channel.setA(Double.parseDouble(parts[5]));
                    channel.setB(Double.parseDouble(parts[6]));
                    channel.setSkew(Double.parseDouble(parts[7]));
                    channel.setMin(parts[8]);
                    channel.setMax(parts[9]);
                    channel.setPrimary(Double.parseDouble(parts[10]));
                    channel.setSecondary(Double.parseDouble(parts[11]));
                    channel.setPs(parts[12]);
                    analogChannels.add(channel);
                } else if (lineNumber >= 2 + analogNum && lineNumber < 2 + analogNum + digitalNum) {
                    // 解析数字通道
                    DigitalChannel channel = new DigitalChannel();
                    channel.setDn(parts[0]);
                    channel.setChId(parts[1]);
                    channel.setPh(parts[2]);
                    channel.setCcbm(parts[3]);
                    channel.setY(parts[4]);
                    digitalChannels.add(channel);
                } else if (lineNumber == 2 + analogNum + digitalNum) {
                    // 名义线路频率
                    lineFrequency = Double.parseDouble(parts[0]);
                    System.out.printf("线路频率: %.2f%n", lineFrequency);
                } else if (lineNumber == 3 + analogNum + digitalNum) {
                    // 采样频率个数
                    nrates = Integer.parseInt(parts[0]);
                    System.out.printf("采样频率个数: %d%n", nrates);
                } else if (lineNumber >= 4 + analogNum + digitalNum && lineNumber < 4 + analogNum + digitalNum + nrates) {
                    // 保存各个采样频率和最后采样
                    double sampFrequency = Double.parseDouble(parts[0]);
                    int endsamp = Integer.parseInt(parts[1]);
                    samplingFrequencies.add(sampFrequency);
                    endsamps.add(endsamp);
                    System.out.printf("采样频率: %.2f, 最后采样点: %d%n", sampFrequency, endsamp);
                } else if (lineNumber == 4 + analogNum + digitalNum + nrates) {
                    // 第一个采样点的日期和时间
                    startTimestamp_date = parts[0];
                    startTimestamp_time = parts[1];
                    System.out.println("起始时间戳: " + startTimestamp_date + " " + startTimestamp_time);
                } else if (lineNumber == 5 + analogNum + digitalNum + nrates) {
                    // 触发点的日期和时间
                    triggerTimestamp_date = parts[0];
                    triggerTimestamp_time = parts[1];
                    System.out.println("触发时间戳: " + triggerTimestamp_date + " " + triggerTimestamp_time);
                } else if (lineNumber == 6 + analogNum + digitalNum + nrates) {
                    // DAT 数据文件格式
                    ft = parts[0];
                    System.out.printf("数据文件格式: %s%n", ft);
                } else if (lineNumber == 7 + analogNum + digitalNum + nrates) {
                    // 时间标记倍乘系数
                    timemult = Double.parseDouble(parts[0]);
                    System.out.printf("时间标记倍乘系数: %.2f%n", timemult);
                }

                lineNumber++;

                // 在读取完所有采样频率和endsamp后进行选取操作
                if (lineNumber == 4 + analogNum + digitalNum + nrates) {
                    selectMaxSamplingFrequencyAndEndsamps();
                }
            }
        }
    }

    // 选取最大的采样频率及其对应的endsamp和前一个endsamp
    private void selectMaxSamplingFrequencyAndEndsamps() {
        if (!samplingFrequencies.isEmpty()) {
            int maxIndex = 0;
            for (int i = 1; i < samplingFrequencies.size(); i++) {
                if (samplingFrequencies.get(i) > samplingFrequencies.get(maxIndex)) {
                    maxIndex = i;
                }
            }

            maxSampFrequency = samplingFrequencies.get(maxIndex);
            maxEndsamp = endsamps.get(maxIndex);

            if (maxIndex > 0) {
                maxPreviousEndsamp = endsamps.get(maxIndex - 1);
            } else {
                maxPreviousEndsamp = 1; // 表示没有前一个endsamp，可根据实际需求调整此默认值
            }
        } else {
            maxSampFrequency = 0;
            maxEndsamp = 0;
            maxPreviousEndsamp = 0;
        }
    }

    // 获取最大采样频率
    public double getMaxSampFrequency() {
        return maxSampFrequency;
    }

    // 获取对应的endsamp
    public int getMaxEndsamp() {
        return maxEndsamp;
    }

    // 获取前一个endsamp
    public int getMaxPreviousEndsamp() {
        return maxPreviousEndsamp;
    }



    // 获取模拟通道列表
    public List<AnalogChannel> getAnalogChannels() {
        return analogChannels;
    }

    // 获取数字通道列表
    public List<DigitalChannel> getDigitalChannels() {
        return digitalChannels;
    }


    // 获取时间标记倍乘系数
    public double getTimeMult() {

        return timemult;
    }




}
