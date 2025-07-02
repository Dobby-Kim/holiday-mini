package com.holidaymini.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {

    @Bean("loaderExecutor")
    public Executor loaderExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(20);
        exec.setMaxPoolSize(40);
        exec.setQueueCapacity(200);
        exec.setThreadNamePrefix("loader-");
        exec.initialize();
        return exec;
    }

}
