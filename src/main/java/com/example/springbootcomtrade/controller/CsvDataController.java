package com.example.springbootcomtrade.controller;

import com.example.springbootcomtrade.pojo.HarmonicAnalysisResult;
import com.example.springbootcomtrade.pojo.VoltageData;
import com.example.springbootcomtrade.service.HarmonicAnalysisService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CsvDataController {

    private HarmonicAnalysisService harmonicAnalysisService;

    @PostMapping("/csvProcessData")
    public ResponseEntity<String> processCsvData(
            // 接收前端上传的CSV文件
            @RequestParam("csvFile") MultipartFile csvFile,
            // 接收前端输入的基波频率
            @RequestParam("lineFrequency") double lineFrequency,
            // 接收前端输入的最大采样频率
            @RequestParam("maxSampleFrequency") double maxSampleFrequency,
            // 接收前端输入的通道名称
            @RequestParam("channelName") String channelName,
            // 添加HttpServletRequest参数
            HttpServletRequest request) {

        // 打印接收到的前端参数信息
        System.out.println("接收到前端请求，基波频率: " + lineFrequency + ", 最大采样频率: " + maxSampleFrequency + ", 通道名称: " + channelName);

        // 新增日志输出，查看参数接收情况
        System.out.println("通道名称参数接收情况：是否成功绑定参数: " + (channelName!= null) + ", 参数来源: " + request.getServletPath());

        // 设置HarmonicAnalysisService的基波频率和最大采样频率属性
        setHarmonicAnalysisService(lineFrequency, maxSampleFrequency);

        // 从上传的CSV文件中读取与指定通道名称相关的电压数据
        VoltageData voltageData = readVoltageDataFromCsv(csvFile, channelName, Charset.forName("GB2312"));
        if (voltageData == null) {
            // 如果读取数据失败，返回BAD_REQUEST状态码给前端
            System.out.println("读取电压数据失败，返回BAD_REQUEST状态码给前端。");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // 提取电压数组u
        List<Double> u = voltageData.getVoltage();
        // 打印U长度
        System.out.println("U长度: " + u.size());
        // 打印u数组内容
        System.out.println("u数组内容:");
        if (u.size() > 0) {
            for (Double value : u) {
                System.out.print(value + " ");
            }
        } else {
            System.out.println("电压数组u为空。");
        }

        // 调用HarmonicAnalysisService的performHarmonicAnalysis方法进行谐波分析
        HarmonicAnalysisResult result = harmonicAnalysisService.performHarmonicAnalysis(voltageData);

        // 分别计算各次谐波电压含有率的95%值
        List<Double> harmonicContent95Percentiles = calculateHarmonicContentPercentiles(result.getHRUh());

        // 输出各次谐波电压含有率的95%值
        System.out.println("各次谐波电压含有率的95%值：");
        for (int i = 0; i < harmonicContent95Percentiles.size(); i++) {
            System.out.println("第 " + (i + 2) + " 次谐波：" + harmonicContent95Percentiles.get(i) + "%");
        }

        // 计算总谐波畸变率（THD）的95%值
        double thd95Percentile = calculatePercentile(result.getTHD(), 95);
        System.out.println("总谐波畸变率（THD）的95%值：" + thd95Percentile + "%");


        // 设置结果对象中的95%值相关属性
        result.setHRUh95Percentiles(harmonicContent95Percentiles);
        result.setTHD95Percentile(thd95Percentile);

        // 将结果格式化为指定样式的字符串
        String formattedResult = result.formatResult();

        // 将格式化后的结果以OK状态码返回给前端
        return new ResponseEntity<>(formattedResult, HttpStatus.OK);
    }

    private void setHarmonicAnalysisService(double lineFrequency, double maxSampleFrequency) {
        if (harmonicAnalysisService == null) {
            harmonicAnalysisService = new HarmonicAnalysisService();
        }
        harmonicAnalysisService.setLineFrequency(lineFrequency);
        harmonicAnalysisService.setFs(maxSampleFrequency);

        // 打印设置后的HarmonicAnalysisService属性信息
        System.out.println("已设置HarmonicAnalysisService，基波频率: " + harmonicAnalysisService.getLineFrequency() + ", 采样频率: " + harmonicAnalysisService.getFs());
    }

    private VoltageData readVoltageDataFromCsv(MultipartFile csvFile, String channelName, Charset charset) {
        VoltageData voltageData = new VoltageData();
        List<Double> voltageList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvFile.getInputStream(), charset))) {
            String line;
            boolean headerSkipped = false;

            // 用于存储CSV文件标题行的列名数组
            String[] columnNames = null;

            // 读取标题行，获取列名数组
            if ((line = reader.readLine())!= null) {
                System.out.println("正在读取CSV文件标题行: " + line);
                columnNames = line.split(",");
                headerSkipped = true;
            }

            // 标记是否已经找到指定通道名称对应的列索引
            boolean columnIndexFound = false;
            int columnIndex = -1;

            // 遍历CSV文件的每一行数据（除标题行外）
            while ((line = reader.readLine())!= null) {
                if (!headerSkipped) {
                    continue;
                }

                String[] values = line.split(",");

                if (!columnIndexFound) {
                    // 根据通道名称找到对应的列索引，并打印相关信息
                    columnIndex = findColumnIndex(columnNames, channelName);
                    System.out.println("正在查找通道名称: " + channelName + ", 找到的列索引: " + columnIndex);
                    if (columnIndex!= -1) {
                        columnIndexFound = true;
                    }
                }

                if (columnIndexFound && columnIndex!= -1) {
                    try {
                        double value = Double.parseDouble(values[columnIndex]);
                        System.out.println("成功解析数据值: " + value);
                        voltageList.add(value);
                    } catch (NumberFormatException e) {
                        System.out.println("解析数据失败，数据值: " + values[columnIndex] + ", 错误信息: " + e.getMessage());
                    }
                }
            }

            voltageData.setVoltage(voltageList);

            // 打印读取到的电压数据信息
            if (voltageList.size() > 0) {
                System.out.println("已成功从CSV文件读取电压数据，数据长度: " + voltageList.size());
            } else {
                System.out.println("从CSV文件读取电压数据为空。");
            }

            return voltageData;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("读取CSV文件时发生IO异常。");
            return null;
        }
    }

    private int findColumnIndex(String[] columnNames, String channelName) {
        channelName = channelName.trim().toLowerCase(); // 清理并转换为小写

        for (int i = 0; i < columnNames.length; i++) {
            String columnName = columnNames[i].trim().toLowerCase(); // 清理并转换为小写
            if (columnName.equals(channelName)) {
                return i;
            }
        }
        return -1;
    }
    // 计算各次谐波电压含有率的95%值的方法
    private List<Double> calculateHarmonicContentPercentiles(List<List<Double>> harmonicContentList) {
        List<Double> percentiles = new ArrayList<>();
        int numHarmonics = harmonicContentList.get(0).size(); // 假设所有窗口的谐波次数相同，获取谐波次数

        for (int harmonicIndex = 0; harmonicIndex < numHarmonics; harmonicIndex++) {
            List<Double> harmonicValues = new ArrayList<>();

            for (List<Double> windowData : harmonicContentList) {
                harmonicValues.add(windowData.get(harmonicIndex));
            }

            Collections.sort(harmonicValues);

            int index = (int) ((95 / 100.0) * harmonicValues.size());
            if (index < harmonicValues.size()) {
                percentiles.add(harmonicValues.get(index));
            } else {
                percentiles.add(0.0);
            }
        }

        return percentiles;
    }

    // 计算一维列表数据的指定百分位数的方法
    private double calculatePercentile(List<Double> dataList, int percentile) {
        Collections.sort(dataList);

        int index = (int) ((percentile / 100.0) * dataList.size());
        if (index < dataList.size()) {
            return dataList.get(index);
        }

        return 0.0;
    }
}