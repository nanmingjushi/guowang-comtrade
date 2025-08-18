package com.example.springbootcomtrade.controller;

import com.example.springbootcomtrade.pojo.AnalogChannel;
import com.example.springbootcomtrade.pojo.SampleData;
import com.example.springbootcomtrade.service.DatDataService;
import com.example.springbootcomtrade.service.CfgDataService;
import com.example.springbootcomtrade.service.OutputCsvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RestController
@RequestMapping("/data")
public class CfgDatDataController {

    private static final Logger logger = LogManager.getLogger(CfgDatDataController.class);

    @Autowired
    private CfgDataService cfgDataService;

    @Autowired
    private DatDataService datDataService;

    @Autowired
    private OutputCsvService outputCsvService;

    @PostMapping("/processData")
    public ResponseEntity<String> processData(@RequestParam("cfgFile") MultipartFile cfgFile,
                                                    @RequestParam("datFile") MultipartFile datFile,
                                                    @RequestParam("csvFileName") String csvFileName) {

        try {
            // 记录处理开始
            logger.info("开始处理数据...");

            // 直接从上传的文件读取 CFG 文件内容
            cfgDataService.readCfgFromMultipart(cfgFile);

            // 获取MaxPreviousEndsamp和MaxEndsamp值
            int previousEndsamp = cfgDataService.getMaxPreviousEndsamp();
            int correspondingEndsamp = cfgDataService.getMaxEndsamp();

            // 设置OutputCsvService中的MaxPreviousEndsamp和MaxEndsamp值
            outputCsvService.setMaxPreviousEndsamp(previousEndsamp);
            outputCsvService.setMaxEndsamp(correspondingEndsamp);

            // 从 DataReadService 读取 DAT 文件
            double timemult = cfgDataService.getTimeMult();
            List<AnalogChannel> analogAdj = cfgDataService.getAnalogChannels();
            List<SampleData> sampleDataList = datDataService.readDatFileFromMultipart(datFile,
                    cfgDataService.getAnalogChannels().size(),
                    cfgDataService.getDigitalChannels().size(),
                    timemult, analogAdj);

            // 生成输出流
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                outputCsvService.writeDataToCsv(outputStream, sampleDataList,
                        cfgDataService.getAnalogChannels(), cfgDataService.getDigitalChannels());

                // 获取输出流中的字节数据
                byte[] csvData = outputStream.toByteArray();
                // 获取桌面路径
                String desktopPath = System.getProperty("user.home") + "/Desktop/";
                // 生成完整的文件路径
                String filePath = Paths.get(desktopPath, csvFileName.endsWith(".csv") ? csvFileName : csvFileName + ".csv").toString();
                // 将字节数据写入文件
                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    fos.write(csvData);
                }

                // 返回成功的响应
                return ResponseEntity.ok("数据处理成功，结果已保存至 " + filePath);

            }
        } catch (IOException e) {
            logger.error("数据处理失败", e);
            // 返回失败的响应
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("数据处理失败：" + e.getMessage());
        }
    }

}
