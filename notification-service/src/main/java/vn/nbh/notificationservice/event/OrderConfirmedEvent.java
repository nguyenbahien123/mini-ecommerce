package vn.nbh.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmedEvent {
    // Chỉ cần OrderId, Notification Service sẽ tự gọi chéo qua FeignClient để lấy chi tiết
    private Long orderId;
}