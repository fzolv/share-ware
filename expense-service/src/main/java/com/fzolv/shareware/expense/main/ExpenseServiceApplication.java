package com.fzolv.shareware.expense.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = "com.fzolv.shareware")
@EnableJpaRepositories(basePackages = "com.fzolv.shareware.data.repository")
@EntityScan(basePackages = "com.fzolv.shareware.data.entity")
public class ExpenseServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpenseServiceApplication.class, args);
    }
}
