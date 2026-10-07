package com.feimao;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 飞猫程序 - 后端服务启动类
 *
 * @author feimao
 * @date 2026-04-06
 */
@SpringBootApplication
@MapperScan("com.feimao.mapper")
@EnableAsync
@EnableScheduling
public class FeimaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeimaoApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("  飞猫程序后端服务启动成功！");
        System.out.println("  API文档地址: http://localhost:8080/doc.html");
        System.out.println("========================================\n");
    }

}
