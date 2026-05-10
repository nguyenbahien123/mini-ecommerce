package vn.nbh.orderservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import vn.nbh.orderservice.dto.response.ApiResponse;
import vn.nbh.orderservice.service.CartService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
@Tag(name = "Cart Controller", description = "Quản lý Giỏ hàng bằng Redis")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Thêm sản phẩm vào giỏ", description = "Lưu trữ trên bộ nhớ RAM Redis siêu tốc")
    @PostMapping("/add")
    public ApiResponse<Void> addToCart(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {

        Integer userId = Math.toIntExact(jwt.getClaim("userId"));
        cartService.addToCart(userId, productId, quantity);

        return ApiResponse.<Void>builder()
                .message("Đã thêm vào giỏ hàng")
                .build();
    }

    @Operation(summary = "Xem giỏ hàng", description = "Trả về Map chứa Product ID và Số lượng")
    @GetMapping
    public ApiResponse<Map<Long, Integer>> getCart(@AuthenticationPrincipal Jwt jwt) {
        Integer userId = Math.toIntExact(jwt.getClaim("userId"));
        return ApiResponse.<Map<Long, Integer>>builder()
                .result(cartService.getCart(userId))
                .build();
    }

    @Operation(summary = "Xóa một sản phẩm khỏi giỏ")
    @DeleteMapping("/remove/{productId}")
    public ApiResponse<Void> removeFromCart(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productId) {

        Integer userId = Math.toIntExact(jwt.getClaim("userId"));
        cartService.removeFromCart(userId, productId);
        return ApiResponse.<Void>builder().message("Đã xóa khỏi giỏ").build();
    }

    @Operation(summary = "Dọn sạch giỏ hàng")
    @DeleteMapping("/clear")
    public ApiResponse<Void> clearCart(@AuthenticationPrincipal Jwt jwt) {
        Integer userId = Math.toIntExact(jwt.getClaim("userId"));
        cartService.clearCart(userId);
        return ApiResponse.<Void>builder().message("Đã dọn sạch giỏ").build();
    }
}