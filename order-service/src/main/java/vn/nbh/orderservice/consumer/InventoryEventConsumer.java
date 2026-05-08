package vn.nbh.orderservice.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import vn.nbh.orderservice.event.InventoryFailedEvent;
import vn.nbh.orderservice.service.OrderService;

@Component
@Slf4j
@RequiredArgsConstructor
public class InventoryEventConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "inventory-failed-topic", groupId = "order-group")
    public void handleInventoryFailed(InventoryFailedEvent event) {
        log.warn("Nhận tín hiệu ROLLBACK từ Product Service cho Đơn hàng ID: {}", event.getOrderId());

        // Tiến hành Hủy Đơn Hàng (Compensating Transaction)
        orderService.cancelOrder(event.getOrderId(), event.getReason());
    }
}