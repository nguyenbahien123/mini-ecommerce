package vn.nbh.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync // Bật tính năng chạy bất đồng bộ cho Spring
public class AsyncConfig {

    @Bean(name = "cartAsyncExecutor")
    public Executor cartAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);      // Luôn duy trì 5 thread trực chiến
        executor.setMaxPoolSize(20);      // Bùng nổ tối đa lên 20 thread khi lượng truy cập cao
        executor.setQueueCapacity(500);   // Nếu 20 thread đều bận, nhét tối đa 500 yêu cầu vào hàng đợi
        executor.setThreadNamePrefix("CartSync-"); // Đặt tên để dễ debug trong Log
        executor.initialize();
        return executor;
    }
}