package vn.nbh.orderservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import vn.nbh.orderservice.enums.PaymentMethod;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    // Tối ưu nhất là lấy trực tiếp từ JWT Token trong SecurityContextHolder
    private Integer userId;

    @NotNull(message = "INVALID_INPUT")
    private PaymentMethod paymentMethod;

    @NotBlank(message = "INVALID_INPUT")
    private String shippingAddress;

    @NotBlank(message = "INVALID_INPUT")
    private String phoneNumber;

    @NotEmpty(message = "INVALID_INPUT")
    @Valid // Kích hoạt validate cho các item bên trong list
    private List<OrderItemRequest> items;
}