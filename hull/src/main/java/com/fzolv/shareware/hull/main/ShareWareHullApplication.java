package com.fzolv.shareware.hull.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.fzolv.shareware")
@EnableJpaRepositories(basePackages = "com.fzolv.shareware.data.repositories")
@EntityScan(basePackages = "com.fzolv.shareware.data.entities")
public class ShareWareHullApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShareWareHullApplication.class, args);
    }
}
