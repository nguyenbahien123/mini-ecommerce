package vn.nbh.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class RateLimiterConfig {
    /**
     * Resolver này sẽ trích xuất địa chỉ IP của Client để làm Key lưu vào Redis.
     * Redis sẽ tạo một key kiểu: "request_rate_limiter.{IP_ADDRESS}"
     */
    @Bean
    public KeyResolver ipKeyResolver(){
        return exchange -> Mono.just(
                Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress()
        );
    }

}
