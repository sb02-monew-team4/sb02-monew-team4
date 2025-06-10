package com.team4.monew.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

  @Bean
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);   // 동시 실행 가능한 스레드 수
    executor.setMaxPoolSize(10);   // 최대 스레드 수
    executor.setQueueCapacity(100); // 큐가 처리할 수 있는 작업 수
    executor.setThreadNamePrefix("AsyncExecutor-");
    executor.initialize();
    return executor;
  }
}