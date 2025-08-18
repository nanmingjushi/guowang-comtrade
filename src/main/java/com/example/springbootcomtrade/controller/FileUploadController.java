package com.example.springbootcomtrade.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author nan chao
 * @date 2024-10-23 15:59
 */


@Controller
public class FileUploadController {



    @GetMapping("/cfg_dat_upload")
    public String cfgDatUpload() {
        return "cfg_dat_upload"; // 返回cfg_dat_upload.html
    }

    @GetMapping("/csv_upload")
    public String CsvUpload() {
        return "csv_upload"; // 返回cfg_dat_upload.html
    }


}
