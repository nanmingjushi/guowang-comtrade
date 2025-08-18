package com.example.springbootcomtrade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;



@SpringBootApplication
@Controller
public class SpringbootComtradeApplication {
    public static void main(String[] args) {

        SpringApplication.run(SpringbootComtradeApplication.class, args);
    }

    @GetMapping({"/","/index"})
    public String index() {
        return "index"; // 转发到 index.html
    }

}
