package vn.nbh.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // Kích hoạt tính năng Web Security cho ứng dụng
@EnableMethodSecurity // Cho phép phân quyền dựa trên Annotation (như @PreAuthorize) ở tầng Service/Controller
public class SecurityConfig {

    private CustomJwtDecoder customJwtDecoder;

    // Danh sách các API "mở", ai cũng có thể truy cập mà không cần Token (Login, Register, Refresh...)
    private final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/user/add", "/api/v1/auth/token", "/api/v1/auth/refresh", "/api/v1/auth/logout", "/api/v1/auth/introspect"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        // 1. Cấu hình phân quyền cho các Request
        httpSecurity.authorizeHttpRequests(request -> request
                .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll() // Cho phép POST vào các endpoint công khai
                .anyRequest().authenticated()); // Tất cả các request khác đều phải xác thực (có Token)

        // 2. Cấu hình ứng dụng đóng vai trò là OAuth2 Resource Server
        httpSecurity.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwtConfigurer -> jwtConfigurer
                        .decoder(customJwtDecoder) // Sử dụng bộ giải mã tùy chỉnh để check Blacklist
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())) // Chuyển đổi Claim từ JWT sang Authority trong Spring
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint())); // Xử lý lỗi khi xác thực thất bại

        // 3. Tắt CSRF (vì chúng ta dùng JWT/Stateless, không dùng Session/Cookie nên không sợ tấn công CSRF)
        httpSecurity.csrf(csrf -> csrf.disable());

        return httpSecurity.build();
    }

    /**
     * Tùy chỉnh cách chuyển đổi từ JWT sang đối tượng Authentication của Spring Security.
     * Mặc định Spring sẽ thêm tiền tố "SCOPE_", hàm này giúp bạn tùy chỉnh hoặc loại bỏ nó.
     */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // Loại bỏ tiền tố mặc định "SCOPE_" để khớp với logic "ROLE_" mà bạn tự viết trong Service
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}