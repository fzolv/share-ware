package com.fzolv.shareware.server.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.fzolv.shareware")
public class ShareWareApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShareWareApplication.class, args);
    }
}
