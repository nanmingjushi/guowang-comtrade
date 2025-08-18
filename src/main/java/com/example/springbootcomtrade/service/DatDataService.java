package com.example.springbootcomtrade.service;

import com.example.springbootcomtrade.pojo.AnalogChannel;
import com.example.springbootcomtrade.pojo.SampleData;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author nan chao
 * @date 2024-10-22 15:42
 */

@Service
public class DatDataService {

    private static final Logger logger = LogManager.getLogger(DatDataService.class);

    //读取已知路径的dat文件，接收字符串路径，后端写
    public List<SampleData> readDatFile(String filePath, int analogNum, int digitalNum, double timeMult, List<AnalogChannel> analogAdj) throws IOException {
        List<SampleData> sampleDataList = new ArrayList<>();
        byte[] data = readFile(filePath);

        int dataLen = data.length;
        int datPackLen = 4 + 4 + (analogNum * 2) + (int) Math.ceil(digitalNum / 16.0) * 2;

        // 解包数据
        for (int pos = 0; pos < dataLen; pos += datPackLen) {
            SampleData sample = unpackData(data, pos, analogNum, digitalNum, timeMult, analogAdj);
            sampleDataList.add(sample);
        }

        // 打印结果
//        for (SampleData sample : sampleDataList) {
//            System.out.print(sample.getN() + ", " + sample.getTimestampUs() + ", ");
//            System.out.print(sample.getAnalogValues() + ", ");
//            System.out.println(sample.getDigitalValues());
//        }

        return sampleDataList;
    }

    //从输入流读取而不是从文件路径读取。接收的是dat文件，MultipartFile对象,前端上传
    public List<SampleData> readDatFileFromMultipart(MultipartFile datFile, int analogChannelSize, int digitalChannelSize, double timeMult, List<AnalogChannel> analogAdj) throws IOException {
        List<SampleData> sampleDataList = new ArrayList<>();

        // 读取上传的 DAT 文件内容
        byte[] data = readFileFromInputStream(datFile.getInputStream());

        int dataLen = data.length;
        int datPackLen = 4 + 4 + (analogChannelSize * 2) + (int) Math.ceil(digitalChannelSize / 16.0) * 2;

        // 解包数据
        for (int pos = 0; pos < dataLen; pos += datPackLen) {
            SampleData sample = unpackData(data, pos, analogChannelSize, digitalChannelSize, timeMult, analogAdj);
            sampleDataList.add(sample);
        }

        return sampleDataList;
    }
    private byte[] readFileFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }

        return byteArrayOutputStream.toByteArray();
    }

    //使用一个固定大小的缓冲区buffer（这里设置为1024字节），
    // 通过循环不断地从FileInputStream中读取数据到缓冲区中。
    // 每次读取操作后，检查读取到的字节数bytesRead，如果不为-1，表示还有数据可读，
    // 就将缓冲区中的数据写入到ByteArrayOutputStream中。
    private byte[] readFile(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fis.read(buffer))!= -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            logger.error("读取文件 {} 时发生异常", filePath, e);
            throw e;
    }
    }
    private SampleData unpackData(byte[] data, int pos, int analogNum, int digitalNum, double timeMult, List<AnalogChannel> analog) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN); // 小端格式

        int n = buffer.getInt(pos); // 采样编号
        int timestampRaw = buffer.getInt(pos + 4); // 原始时间标记
        long timestampUs = (timestampRaw >= 0x80000000) ? -((timestampRaw ^ 0xFFFFFFFF) + 1) : timestampRaw;
        timestampUs *= timeMult; // 应用倍乘系数

        SampleData sample = new SampleData(n, timestampUs);

        // 解析模拟通道数据
        for (int i = 0; i < analogNum; i++) {
            short analogValue = buffer.getShort(pos + 8 + (i * 2));
            // 应用增益和偏移系数
            double adjustedValue = analogValue * analog.get(i).getA() + analog.get(i).getB();
            // 应用一次侧/二次侧比率
            if (analog.get(i).getPs().equals("P"))
                adjustedValue *= analog.get(i).getPrimary() / analog.get(i).getSecondary();
            sample.addAnalogValue(adjustedValue);
        }

        // 解析数字通道数据
        for (int i = 0; i < digitalNum; i++) {
            short digitalChunk = buffer.getShort(pos + 8 + (analogNum * 2) + (i / 16) * 2);
            int bitOffset = i % 16;
            int digitalValue = (digitalChunk >> (15 - bitOffset)) & 1;
            sample.addDigitalValue(digitalValue);
        }

        return sample;
    }
}
