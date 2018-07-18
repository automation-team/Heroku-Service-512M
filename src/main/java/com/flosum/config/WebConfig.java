package com.flosum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
//import org.springframework.aop.interceptor.CustomAsyncExceptionHandler;

import com.flosum.config.CustomAsyncExceptionHandler;

@EnableWebMvc
@Configuration
@EnableAsync
@EnableScheduling
@ComponentScan("com.flosum")
public class WebConfig extends WebMvcConfigurerAdapter implements AsyncConfigurer, SchedulingConfigurer {
	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(10);
		executor.setMaxPoolSize(10);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("Git-");
		executor.initialize();
		return executor;
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new CustomAsyncExceptionHandler();
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setScheduler(taskExecutor()); 
		
	}

	@Bean(destroyMethod="shutdown")
	public Executor taskExecutor() {
	    return Executors.newScheduledThreadPool(10);
	}


}
