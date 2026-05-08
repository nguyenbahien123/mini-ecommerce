package vn.nbh.orderservice.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import vn.nbh.orderservice.event.PaymentFailedEvent;
import vn.nbh.orderservice.event.PaymentSuccessEvent;
import vn.nbh.orderservice.service.OrderService;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderService orderService;

    // 1. Hàm hứng Event THẤT BẠI (Bạn đã code ở bước trước)
    @KafkaListener(topics = "payment-failed-topic", groupId = "order-group")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.warn("Nhận thông báo THANH TOÁN THẤT BẠI cho Đơn hàng ID: {}. Lý do: {}", event.getOrderId(), event.getReason());
        orderService.cancelOrderDueToPaymentFailure(event.getOrderId(), event.getReason());
    }

    // 2. Hàm MỚI: Hứng Event THÀNH CÔNG
    @KafkaListener(topics = "payment-success-topic", groupId = "order-group")
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("Nhận thông báo THANH TOÁN THÀNH CÔNG từ PayOS cho Đơn hàng ID: {}", event.getOrderId());

        // Gọi service để cập nhật trạng thái đơn hàng sang CONFIRMED
        orderService.confirmOrder(event.getOrderId());
    }


}