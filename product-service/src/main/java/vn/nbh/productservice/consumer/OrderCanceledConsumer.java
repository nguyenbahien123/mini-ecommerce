package vn.nbh.productservice.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import vn.nbh.productservice.event.OrderCanceledEvent;
import vn.nbh.productservice.service.ProductService;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderCanceledConsumer {

    private final ProductService productService;

    @KafkaListener(topics = "order-canceled-topic", groupId = "product-group")
    public void handleOrderCanceled(OrderCanceledEvent event) {
        log.warn("Nhận yêu cầu HOÀN KHO do Đơn hàng ID: {} bị hủy. Lý do: {}", event.getOrderId(), event.getReason());

        try {
            // Gọi hàm cộng kho
            productService.restoreInventory(event.getItems());
        } catch (Exception e) {
            log.error("Lỗi khi hoàn kho cho Đơn hàng {}: {}", event.getOrderId(), e.getMessage());
            // Throw ra để Spring Kafka tự động kích hoạt Retry (nếu bị lock DB)
            throw e;
        }
    }
}