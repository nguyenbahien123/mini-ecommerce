package vn.nbh.orderservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import vn.nbh.orderservice.dto.request.OrderRequest;
import vn.nbh.orderservice.dto.response.ApiResponse;
import vn.nbh.orderservice.dto.response.OrderResponse;
import vn.nbh.orderservice.service.OrderService;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<OrderResponse> createOrder(
            @RequestBody @Valid OrderRequest request,
            @AuthenticationPrincipal Jwt jwt // Spring tự động tiêm đối tượng Jwt vào đây
    ) {
        // Lấy userId từ Custom Claim
        // Lưu ý: Thư viện parse số mặc định thường để ở dạng Long, nên cần ép kiểu cẩn thận
        Long rawUserId = jwt.getClaim("userId");
        Integer userId = Math.toIntExact(rawUserId);

        // Ghi đè userId từ token vào request để tránh việc FE gửi sai/giả mạo userId
        request.setUserId(userId);

        log.info("Nhận yêu cầu tạo đơn hàng từ User ID: {}", request.getUserId());
        OrderResponse result = orderService.createOrder(request);

        return ApiResponse.<OrderResponse>builder()
                .message("Tạo đơn hàng thành công (Chờ xử lý)")
                .result(result)
                .build();
    }
}