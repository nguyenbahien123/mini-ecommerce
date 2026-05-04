package vn.nbh.userservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import vn.nbh.userservice.dto.response.ApiResponse;
import vn.nbh.userservice.exception.ErrorCode;

import java.io.IOException;

/**
 * Lớp này được gọi khi một request không được xác thực thành công (lỗi 401).
 */
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // Định nghĩa mã lỗi mặc định là UNAUTHENTICATED (Chưa xác thực)
        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;

        // Thiết lập Header và Status code cho Response
        response.setStatus(errorCode.getStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Tạo body phản hồi theo chuẩn ApiResponse của dự án
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        // Sử dụng ObjectMapper để chuyển đổi Object sang chuỗi JSON và ghi vào Response stream
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));

        // Đẩy dữ liệu ra ngoài và kết thúc response
        response.flushBuffer();
    }
}