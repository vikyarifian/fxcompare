package com.example.fxcompare;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class Application {

    @PostConstruct
    public void init() {
        // Workaround for Spring Boot 3.3 timezone mapping limitation where LocalTime ignores JVM local timezone
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Jakarta"));
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
