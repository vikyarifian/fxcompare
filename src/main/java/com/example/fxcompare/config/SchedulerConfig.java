package com.example.fxcompare.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.TimeZone;

@Configuration
public class SchedulerConfig {

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("FxScheduler-");
        
        // Workaround for Spring Boot 3.3 limitation where ThreadPoolTaskScheduler does not automatically inherit JVM default timezone for cron tasks
        scheduler.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));
        
        scheduler.initialize();
        return scheduler;
    }
}
