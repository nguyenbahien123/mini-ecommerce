package vn.nbh.apigateway.filter;

import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
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
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final ReactiveStringRedisTemplate redisTemplate;

    private final List<String> publicEndpoints = List.of(
            "/api/v1/auth/token",
            "/api/v1/auth/introspect",
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout",
            "/api/v1/users/add",
            "/api/v1/payments/webhook.*"
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

        // Cắt bỏ chữ "Bearer " để lấy đúng chuỗi JWT
        String token = authHeader.get(0).substring(7);

        try {
            // 3. Parse Token siêu tốc để lấy JWT ID (jti)
            // Chú ý: Ở đây ta KHÔNG verify chữ ký (để tiết kiệm CPU cho Gateway).
            // Ta chỉ bóc payload ra đọc JTI. Việc verify chữ ký là nhiệm vụ của Microservice phía sau.
            SignedJWT signedJWT = SignedJWT.parse(token);
            String jti = signedJWT.getJWTClaimsSet().getJWTID();

            // 4. Kiểm tra trong Redis xem Token này có nằm trong Blacklist (Logout) không?
            // Dùng flatMap vì đây là cơ chế luồng Reactive không đồng bộ
            return redisTemplate.hasKey(jti)
                    .flatMap(isBlacklisted -> {
                        if (Boolean.TRUE.equals(isBlacklisted)) {
                            log.warn("CẢNH BÁO: Token ID {} đã bị LOGOUT nhưng vẫn cố truy cập vào {}", jti, path);
                            return unauthenticatedResponse(exchange.getResponse());
                        }

                        // Token sạch (không có trong Redis), cho phép đi qua
                        return chain.filter(exchange);
                    });

        } catch (Exception e) {
            log.error("Lỗi parse JWT hoặc Token sai định dạng tại Gateway: {}", e.getMessage());
            return unauthenticatedResponse(exchange.getResponse());
        }
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
