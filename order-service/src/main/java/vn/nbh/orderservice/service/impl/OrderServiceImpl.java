package vn.nbh.orderservice.service.impl;

import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vn.nbh.orderservice.client.ProductClient;
import vn.nbh.orderservice.dto.request.OrderItemRequest;
import vn.nbh.orderservice.dto.request.OrderRequest;
import vn.nbh.orderservice.dto.response.ApiResponse;
import vn.nbh.orderservice.dto.response.OrderDetailResponse;
import vn.nbh.orderservice.dto.response.OrderResponse;
import vn.nbh.orderservice.dto.response.ProductResponse;
import vn.nbh.orderservice.entity.Order;
import vn.nbh.orderservice.entity.OrderDetail;
import vn.nbh.orderservice.enums.OrderStatus;
import vn.nbh.orderservice.event.OrderCreatedEvent;
import vn.nbh.orderservice.exception.AppException;
import vn.nbh.orderservice.exception.ErrorCode;
import vn.nbh.orderservice.repository.OrderRepository;
import vn.nbh.orderservice.service.OrderService;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        // 1. Khởi tạo Order trống
        Order order = Order.builder()
                .userId(request.getUserId())
                .status(OrderStatus.PENDING) // Luôn là PENDING khi vừa tạo
                .paymentMethod(request.getPaymentMethod())
                .shippingAddress(request.getShippingAddress())
                .phoneNumber(request.getPhoneNumber())
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        // 2. Duyệt qua từng Item FE gửi lên, lấy giá thật từ Product Service
        for (OrderItemRequest itemReq : request.getItems()) {
            ProductResponse product;
            try {
                // Gọi API chéo sang Product Service
                ApiResponse<ProductResponse> productApiRes = productClient.getProductById(itemReq.getProductId());
                product = productApiRes.getResult();
            } catch (FeignException e) {
                log.error("Lỗi khi gọi Product Service: ", e);
                throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
            }

            // Kiểm tra tồn kho sơ bộ (Phòng hờ hết hàng sớm)
            if (product.getStockQuantity() < itemReq.getQuantity()) {
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
            }

            // Tính tiền = Giá thật (Product Service) * Số lượng (FE gửi)
            BigDecimal subTotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(subTotal);

            // Tạo Order Detail
            OrderDetail orderDetail = OrderDetail.builder()
                    .productId(product.getId())
                    .quantity(itemReq.getQuantity())
                    .price(product.getPrice()) // LƯU CỨNG GIÁ TẠI THỜI ĐIỂM MUA
                    .subTotal(subTotal)
                    .build();

            order.addOrderDetail(orderDetail);
        }

        // Cập nhật tổng tiền cuối cùng
        order.setTotalAmount(totalAmount);

        // 3. Lưu vào DB (Vì cấu hình CascadeType.ALL, nó sẽ tự lưu các order_details theo)
        order = orderRepository.save(order);

        // 4. Sau khi lưu thành công, bắn Kafka để product-service trừ kho
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(order.getOrderId())
                .items(order.getOrderDetails().stream()
                        .map(d -> new OrderCreatedEvent.OrderItemEvent(d.getProductId(), d.getQuantity()))
                        .collect(Collectors.toList()))
                .build();

        // Gửi vào Topic tên là "order-created-topic"
        kafkaTemplate.send("order-created-topic", String.valueOf(order.getOrderId()), event);
        log.info("Đã bắn event tạo đơn hàng {} vào Kafka", order.getOrderId());

        return mapToResponse(order);
    }

    // --- Các hàm tiện ích map dữ liệu ---
    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getOrderId())
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .shippingAddress(order.getShippingAddress())
                .phoneNumber(order.getPhoneNumber())
                .createdAt(order.getCreatedAt())
                .items(order.getOrderDetails().stream()
                        .map(this::mapToDetailResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private OrderDetailResponse mapToDetailResponse(OrderDetail detail) {
        return OrderDetailResponse.builder()
                .id(detail.getOrderDetailId())
                .productId(detail.getProductId())
                .quantity(detail.getQuantity())
                .price(detail.getPrice())
                .subTotal(detail.getSubTotal())
                .build();
    }
}