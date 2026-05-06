package vn.nbh.productservice.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.stereotype.Component;
import vn.nbh.productservice.event.OrderCreatedEvent;
import vn.nbh.productservice.service.ProductService;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final ProductService productService;

    @KafkaListener(topics = "order-created-topic", groupId = "product-group")
    public void handleOrderCreated(OrderCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Nhận event trừ kho cho Đơn hàng ID: {}. Đọc từ Topic: {}", event.getOrderId(), topic);

        try {
            // Hàm deductInventory đã được gắn @Transactional và Optimistic Locking
            productService.deductInventory(event.getItems());

            log.info("Trừ kho thành công cho Đơn hàng ID: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Lỗi khi trừ kho đơn hàng {}: {}", event.getOrderId(), e.getMessage());
            // Bắt buộc phải throw ra để Kafka biết là lỗi và tiến hành Retry
            throw e;
        }
    }

}