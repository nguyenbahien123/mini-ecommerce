package vn.nbh.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final List<String> publicEndpoints = List.of(
            "/api/v1/auth/token",
            "/api/v1/auth/introspect",
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout",
            "/api/v1/users/add"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        log.info("Incoming request: {} {}", request.getMethod(), path);

        // 1. Nếu là endpoint public, cho đi qua luôn
        if(isPublicEndpoint(path)) {
            log.info("Public endpoint accessed: {}", path);
            return chain.filter(exchange);
        }

        // 2. Nếu không phải public, bắt buộc phải có Header Authorization
        List<String> authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (CollectionUtils.isEmpty(authHeader) || !authHeader.get(0).startsWith("Bearer ")) {
            log.warn("Request bị Gateway từ chối vì thiếu Token: {}", path);
            return unauthenticatedResponse(exchange.getResponse());
        }

        // 3. (Tùy chọn) Gateway có thể gọi sang User Service (Identity Service) để check xem Token có bị Blacklist không.
        // Ở giai đoạn này, ta cứ tin tưởng là có Token thì cho đi tiếp. Xuống dưới các service con sẽ tự decode và kiểm tra kỹ hơn.

        return chain.filter(exchange);
    }

    private boolean isPublicEndpoint(String path) {
        return publicEndpoints.stream().anyMatch(path::matches);
    }

    // Xử lý response trả về lỗi 401 Unauthenticated chuẩn JSON
    private Mono<Void> unauthenticatedResponse(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Bạn có thể format lại chuỗi JSON này cho khớp với cấu trúc ApiResponse của bạn
        String body = "{\"code\": 1001, \"message\": \"Unauthenticated (Bị chặn tại Gateway)\"}";

        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    // Xác định thứ tự chạy của Filter (số càng nhỏ chạy càng sớm)
    @Override
    public int getOrder() {
        return -1;
    }
}
