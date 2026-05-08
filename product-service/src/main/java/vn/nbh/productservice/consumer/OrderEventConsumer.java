package vn.nbh.productservice.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import vn.nbh.productservice.event.InventoryDeductedEvent;
import vn.nbh.productservice.event.InventoryFailedEvent;
import vn.nbh.productservice.event.OrderCreatedEvent;
import vn.nbh.productservice.service.ProductService;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final ProductService productService;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @RetryableTopic(attempts = "5", backOff = @BackOff(delay = 1000, multiplier = 2.0),include = {
            ObjectOptimisticLockingFailureException.class
    })
    @KafkaListener(topics = "order-created-topic", groupId = "product-group")
    public void handleOrderCreated(OrderCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Nhận event trừ kho cho Đơn hàng ID: {}. Đọc từ Topic: {}", event.getOrderId(), topic);

        try {
            // Hàm deductInventory đã được gắn @Transactional và Optimistic Locking
            productService.deductInventory(event.getItems());

            log.info("Trừ kho thành công cho Đơn hàng ID: {}", event.getOrderId());

            InventoryDeductedEvent deductedEvent = new InventoryDeductedEvent(
                    event.getOrderId(),
                    event.getTotalAmount());

            kafkaTemplate.send("inventory-deducted-topic", String.valueOf(event.getOrderId()), deductedEvent);
            log.info("Đã bắn event yêu cầu thanh toán cho Đơn hàng ID: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("Lỗi khi trừ kho đơn hàng {}: {}", event.getOrderId(), e.getMessage());
            // Bắt buộc phải throw ra để Kafka biết là lỗi và tiến hành Retry
            throw e;
        }
    }

    @DltHandler
    public void processDltMessage(OrderCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Exception exception) {
        log.error("====== SAGA ROLLBACK TRIGGERED ======");
        log.error("Trừ kho thất bại hoàn toàn cho Đơn hàng ID: {}. Lý do: {}", event.getOrderId(),
                exception.getMessage());

        // 1. Tạo Event thông báo thất bại
        InventoryFailedEvent failedEvent = InventoryFailedEvent.builder()
                .orderId(event.getOrderId())
                .reason(exception.getMessage())
                .build();

        // 2. Gửi Event này sang Order Service để Order Service thực hiện Rollback Saga
        kafkaTemplate.send("inventory-failed-topic", String.valueOf(event.getOrderId()), failedEvent);
        log.info("Đã bắn tín hiệu ROLLBACK (inventory-failed-topic) cho Order Service cho Đơn hàng ID: {}",
                event.getOrderId());
    }

}