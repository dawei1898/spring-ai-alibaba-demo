package com.alibaba.ai.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class SpringAiAlibabaDemoApp {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiAlibabaDemoApp.class, args);
        log.info("========== SPRING-AI-ALIBABA-DEMO-APP SUCCESS TO START ==========");
    }

}
