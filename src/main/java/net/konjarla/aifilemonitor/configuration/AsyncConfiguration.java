package net.konjarla.aifilemonitor.configuration;

import net.konjarla.aifilemonitor.monitoring.model.TaskStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableAsync
public class AsyncConfiguration {
    @Bean(name = "scanTaskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Scan-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "watcherTaskExecutor")
    public TaskExecutor watcherExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Watcher-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "indexTaskExecutor")
    public TaskExecutor indexExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Index-");
        executor.initialize();
        return executor;
    }

    @Bean
    public ConcurrentHashMap<String, TaskStatus> taskStatuses() {
        return new ConcurrentHashMap<>();
    }
}
