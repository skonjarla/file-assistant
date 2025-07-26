package net.konjarla.aifilemonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AiFileMonitorApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiFileMonitorApplication.class, args);
    }
} 