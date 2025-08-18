package com.example.springbootcomtrade.service;

import com.example.springbootcomtrade.pojo.HarmonicAnalysisResult;
import com.example.springbootcomtrade.pojo.VoltageData;
import lombok.Getter;
import lombok.Setter;
import org.jtransforms.fft.DoubleFFT_1D;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Service
public class HarmonicAnalysisService {

    private double lineFrequency;
    private double fs;

    public HarmonicAnalysisService() {
    }

    public HarmonicAnalysisResult performHarmonicAnalysis(VoltageData voltageData) {
        HarmonicAnalysisResult result = new HarmonicAnalysisResult();

        // 打印基波频率
        System.out.println();
        System.out.println("基波频率: " + lineFrequency + "Hz");

        // 打印采样频率
        System.out.println("采样频率: " + fs + "Hz");


        // 获取与指定通道名称对应的那一列电压数据
        List<Double> voltage = voltageData.getVoltage();
        int n = voltage.size();

        // 时间窗口相关计算
        double tWindow = 1 / lineFrequency;
        // 打印t_window
        System.out.println("t_window=" + tWindow);
        int nWindow = (int) (tWindow * fs);
        // 打印n_window
        System.out.println("n_window=" + nWindow);


        // 窗口步长
        int windowStepSize = 1;

        // 滑动时间窗起始索引数组
        List<Integer> startIndices = new ArrayList<>();
        for (int i = 0; i < voltage.size() - nWindow; i += windowStepSize) {
            startIndices.add(i);
        }

//        // 打印第一个窗口内的各个采样点的值
//        if (!startIndices.isEmpty()) {
//            int firstWindowStart = startIndices.get(0);
//            int firstWindowEnd = firstWindowStart + nWindow;
//
//            System.out.println("第一个窗口内的各个采样点的值：");
//            for (int i = firstWindowStart; i < firstWindowEnd; i++) {
//                System.out.println("采样点 " + i + " 的值：" + voltage.get(i));
//            }
//        }



        for (int start : startIndices) {
            int end = start + nWindow;

            // 提取当前窗口内的信号
            List<Double> windowedVoltage = voltage.subList(start, end);
            double[] windowedVoltageArray = windowedVoltage.stream().mapToDouble(Double::doubleValue).toArray();

//

            // 使用JTransforms进行FFT操作
            DoubleFFT_1D fft = new DoubleFFT_1D(windowedVoltageArray.length);
            double[] complexData = new double[2 * windowedVoltageArray.length];
            for (int i = 0; i < windowedVoltageArray.length; i++) {
                complexData[2 * i] = windowedVoltageArray[i];
            }
            fft.complexForward(complexData);

            // 时间轴计算（这里可根据实际需求添加具体注释，示例中暂未详细展开）
            double[] freq = calculateFrequencyAxis(nWindow, fs);

            // 计算谐波频率和对应索引
            double[] harmonicFrequency = calculateHarmonicFrequency(lineFrequency, fs);
            List<Integer> harmonicIndices = calculateHarmonicIndices(harmonicFrequency, nWindow, fs);

            // 计算正频率部分的谐波幅值
            List<Double> harmonicVoltage = calculateHarmonicAmplitude(complexData, harmonicIndices);

            // 基波电压有效值
            int fundamentalIndex = findFundamentalIndex(freq, lineFrequency);

            // 修改：正确获取基波电压对应的复数数据数组
            double[] complexDataAtFundamentalIndex = new double[]{complexData[2 * fundamentalIndex], complexData[2 * fundamentalIndex + 1]};
            double voltageRmsBase = calculateVoltageRms(complexDataAtFundamentalIndex);

            // 计算谐波电压的有效值（RMS）
            List<Double> harmonicVoltagesRms = calculateHarmonicVoltagesRms(complexData, harmonicIndices);

            // 计算谐波电压含有率
            List<Double> harmonicVoltageContent = calculateHarmonicVoltageContent(harmonicVoltagesRms, voltageRmsBase);

            // 计算总谐波畸变率THD
            double thd = calculateTHD(harmonicVoltagesRms, voltageRmsBase);

            // 输出当前窗口内各次谐波电压含有率和THD
            System.out.println("当前窗口（起始索引：" + start + "，结束索引：" + end + "）的计算结果：");
            System.out.println("各次谐波电压含有率：");
            for (int i = 0; i < harmonicVoltageContent.size(); i++) {
                System.out.print("第 " + (i + 2) + " 次谐波：" + harmonicVoltageContent.get(i) + "% ");
            }
            System.out.println();
            System.out.println("总谐波畸变率（THD）：" + thd + "%");


            // 存储结果
            result.getHRUh().add(harmonicVoltageContent);
            result.getTHD().add(thd);
        }

        return result;
    }





    private double[] calculateFrequencyAxis(int n, double samplingFrequency) {
        double[] freq = new double[n / 2];
        for (int i = 0;  i < n / 2; i++) {
            freq[i] = i * samplingFrequency / n;
        }
        return freq;
    }

    private double[] calculateHarmonicFrequency(double lineFrequency, double samplingFrequency) {
        int numHarmonics = (int) ((samplingFrequency / lineFrequency) / 2);
        double[] harmonicFrequency = new double[numHarmonics - 1];
        for (int i = 2; i < numHarmonics + 1; i++) {
            harmonicFrequency[i - 2] = i * lineFrequency;
        }
        return harmonicFrequency;
    }

    private List<Integer> calculateHarmonicIndices(double[] harmonicFrequency, int n, double samplingFrequency) {
        List<Integer> harmonicIndices = new ArrayList<>();
        for (double hf : harmonicFrequency) {
            harmonicIndices.add((int) ((hf * n) / samplingFrequency));
        }
        return harmonicIndices;
    }

    // 谐波幅值
    private List<Double> calculateHarmonicAmplitude(double[] complexData, List<Integer> harmonicIndices) {
        List<Double> harmonicVoltage = new ArrayList<>();
        for (int idx : harmonicIndices) {
            if (idx <= complexData.length / 2) {
                double realPart = complexData[2 * idx];
                double imagPart = complexData[2 * idx + 1];
                double amplitude = Math.sqrt(realPart * realPart + imagPart * imagPart);
                harmonicVoltage.add(amplitude);
            }
        }
        return harmonicVoltage;
    }

    private int findFundamentalIndex(double[] freq, double lineFrequency) {
        int fundamentalIndex = 0;
        double minDiff = Double.MAX_VALUE;
        for (int i = 0; i < freq.length; i = i + 1) {
            if (i!= 0) {
                double diff = Math.abs(freq[i] - lineFrequency);
                if (diff < minDiff) {
                    minDiff = diff;
                    fundamentalIndex = i;
                }
            }
        }
        return fundamentalIndex;
    }

    // 基波电压有效值
    private double calculateVoltageRms(double[] complexData) {
        double realPart = complexData[0];
        double imagPart = complexData[1];
        double magnitude = Math.sqrt(realPart * realPart + imagPart * imagPart);
        return magnitude / Math.sqrt(2);
    }

    private List<Double> calculateHarmonicVoltagesRms(double[] fftVoltage, List<Integer> harmonicIndices) {
        List<Double> harmonicVoltagesRms = new ArrayList<>();
        for (int idx : harmonicIndices) {
            if (idx <= fftVoltage.length / 2) {
                double realPart = fftVoltage[2 * idx];
                double imagPart = fftVoltage[2 * idx + 1];
                double magnitude = Math.sqrt(realPart * realPart + imagPart * imagPart);
                harmonicVoltagesRms.add(magnitude / Math.sqrt(2));
            }
        }
        return harmonicVoltagesRms;
    }


    private List<Double> calculateHarmonicVoltageContent(List<Double> harmonicVoltagesRms, double voltageRmsBase) {
        List<Double> harmonicVoltageContent = new ArrayList<>();
        for (double voltageRms : harmonicVoltagesRms) {
            double content = (voltageRms / voltageRmsBase) * 100;
            // 保留两位小数
            content = Math.round(content * 100.0) / 100.0;
            harmonicVoltageContent.add(content);

        }
        return harmonicVoltageContent;
    }

    private double calculateTHD(List<Double> harmonicVoltagesRms, double voltageRmsBase) {
        double sumOfSquaresHarmonics = 0;
        for (double harmonic : harmonicVoltagesRms) {
            sumOfSquaresHarmonics += Math.pow(harmonic, 2);
        }
        double thd = Math.sqrt(sumOfSquaresHarmonics) / voltageRmsBase * 100;
        // 保留两位小数
        return Math.round(thd * 100.0) / 100.0;

    }
}