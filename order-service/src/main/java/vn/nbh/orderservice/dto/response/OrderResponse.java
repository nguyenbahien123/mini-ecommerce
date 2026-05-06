package vn.nbh.orderservice.dto.response;

import lombok.*;
import vn.nbh.orderservice.enums.OrderStatus;
import vn.nbh.orderservice.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private Integer userId;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private String shippingAddress;
    private String phoneNumber;
    private LocalDateTime createdAt;
    private List<OrderDetailResponse> items;
}