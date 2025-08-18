package com.example.springbootcomtrade.service;

import com.example.springbootcomtrade.pojo.AnalogChannel;
import com.example.springbootcomtrade.pojo.DigitalChannel;
import com.example.springbootcomtrade.pojo.SampleData;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.List;

/**
 * @author nan chao
 * @date 2024-10-22 15:51
 */

@Service
public class OutputCsvService {

    private int maxPreviousEndsamp;
    private int maxEndsamp;

    // 设置maxPreviousEndsamp值的方法
    public void setMaxPreviousEndsamp(int value) {
        maxPreviousEndsamp = value;
    }

    // 设置maxEndsamp值的方法
    public void setMaxEndsamp(int value) {
        maxEndsamp = value;
    }

    // 修改方法，接受 OutputStream 参数
    public void writeDataToCsv(OutputStream outputStream, List<SampleData> sampleDataList,
                               List<AnalogChannel> analogChannels, List<DigitalChannel> digitalChannels) {
        try (BufferedWriter csvWriter = new BufferedWriter(new OutputStreamWriter(outputStream, Charset.forName("GB2312")))) {

            // 写入标题行
            csvWriter.append("n,timestamp_μs");
            for (AnalogChannel channel : analogChannels) {
                csvWriter.append(",").append(channel.getChId());
            }
            for (DigitalChannel channel : digitalChannels) {
                csvWriter.append(",").append(channel.getChId());
            }
            csvWriter.append("\n");


            // 筛选并写入指定区间的数据
            for (int i = Math.max(0, maxPreviousEndsamp); i < maxEndsamp && i < sampleDataList.size(); i++) {
                SampleData sample = sampleDataList.get(i);

                csvWriter.append(String.valueOf(sample.getN())).append(",")
                        .append(String.valueOf(sample.getTimestampUs()));

                // 写入模拟通道值，保留小数点后6位
                DecimalFormat df = new DecimalFormat("#.######");
                for (double analogValue : sample.getAnalogValues()) {
                    csvWriter.append(",").append(df.format(analogValue));
                }
                // 写入数字通道值，保留小数点后6位
                for (double digitalValue : sample.getDigitalValues()) {
                    csvWriter.append(",").append(df.format(digitalValue));
                }
                csvWriter.append("\n");
            }
            System.out.println("数据已成功写入流中。");
        } catch (IOException e) {
            System.err.println("写入CSV文件时发生错误: " + e.getMessage());
        }
    }


}
