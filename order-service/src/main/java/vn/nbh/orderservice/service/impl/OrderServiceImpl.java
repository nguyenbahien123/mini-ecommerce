package vn.nbh.orderservice.service.impl;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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
import vn.nbh.orderservice.event.OrderCanceledEvent;
import vn.nbh.orderservice.event.OrderConfirmedEvent;
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
    private final CartServiceImpl cartService;

    @Override
    @Transactional
    // Đặt tên instance phải khớp với tên trong file application.yml
    @CircuitBreaker(name = "productClient", fallbackMethod = "fallbackGetProduct")
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
                .totalAmount(order.getTotalAmount())
                .items(order.getOrderDetails().stream()
                        .map(d -> new OrderCreatedEvent.OrderItemEvent(d.getProductId(), d.getQuantity()))
                        .collect(Collectors.toList()))
                .build();

        // Gửi vào Topic tên là "order-created-topic"
        kafkaTemplate.send("order-created-topic", String.valueOf(order.getOrderId()), event);
        log.info("Đã bắn event tạo đơn hàng {} vào Kafka", order.getOrderId());

        // Dọn sạch giỏ hàng trong Redis sau khi chốt đơn thành công
        cartService.clearCart(request.getUserId());
        log.info("Đã xóa giỏ hàng trong Redis cho User ID: {}", request.getUserId());
        return mapToResponse(order);
    }

    // Bổ sung hàm này vào OrderServiceImpl
    @Override
    @Transactional
    public void cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // Chỉ Hủy nếu đơn hàng đang PENDING
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CANCELLED);
            // Nếu bạn có cột 'note' hoặc 'cancel_reason' trong bảng Orders thì lưu reason vào đây
            orderRepository.save(order);
            log.info("Đã HỦY đơn hàng ID: {}. Lý do: {}", orderId, reason);
        } else {
            log.warn("Không thể hủy đơn hàng ID: {} vì trạng thái hiện tại là: {}", orderId, order.getStatus());
        }
    }

    @Override
    @Transactional
    public void cancelOrderDueToPaymentFailure(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // Chỉ xử lý nếu đơn hàng đang ở trạng thái PENDING
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CANCELLED);
            order = orderRepository.save(order);
            log.info("Đã HỦY đơn hàng ID: {}. Lý do: {}", orderId, reason);

            // Duyệt qua từng item trong đơn hàng và đẩy ngược lại vào giỏ hàng của User
            for (OrderDetail detail : order.getOrderDetails()) {
                cartService.addToCart(
                        order.getUserId(),
                        detail.getProductId(),
                        detail.getQuantity()
                );
            }
            log.info("Đã khôi phục giỏ hàng Redis cho User ID: {} từ Đơn hàng ID: {}", order.getUserId(), orderId);

            // BẮN EVENT CHUYỀN CHO PRODUCT SERVICE ĐỂ CỘNG KHO
            OrderCanceledEvent canceledEvent = OrderCanceledEvent.builder()
                    .orderId(order.getOrderId())
                    .reason(reason)
                    .items(order.getOrderDetails().stream().map(detail ->
                            OrderCanceledEvent.OrderItemEvent.builder()
                                    .productId(detail.getProductId())
                                    .quantity(detail.getQuantity())
                                    .build()
                    ).collect(Collectors.toList()))
                    .build();

            kafkaTemplate.send("order-canceled-topic", String.valueOf(order.getOrderId()), canceledEvent);
            log.info("Đã bắn tín hiệu order-canceled-topic cho Product Service để hoàn kho.");


        } else {
            log.warn("Bỏ qua yêu cầu hủy. Đơn hàng ID: {} đang ở trạng thái: {}", orderId, order.getStatus());
        }
    }

    @Override
    @Transactional
    public void confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // Chỉ cập nhật nếu đơn hàng đang ở trạng thái PENDING
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            log.info("Đã CẬP NHẬT đơn hàng ID: {} sang trạng thái CONFIRMED (Thanh toán thành công).", orderId);

            // Bắn Kafka báo cho Notification Service gửi Email
            OrderConfirmedEvent event = new OrderConfirmedEvent(orderId);
            kafkaTemplate.send("order-confirmed-topic", String.valueOf(orderId), event);
            log.info("Đã bắn event (order-confirmed-topic) yêu cầu Notification Service gửi Email cho Đơn hàng ID: {}", orderId);

        } else {
            log.warn("Bỏ qua yêu cầu xác nhận. Đơn hàng ID: {} đang ở trạng thái: {}", orderId, order.getStatus());
        }
    }

    // --- HÀM FALLBACK ---
    // Hàm này sẽ tự động được chạy nếu Product Service bị sập hoặc quá 2 giây không phản hồi
    public OrderResponse fallbackGetProduct(OrderRequest request, Exception ex) {
        log.error("CẦU DAO ĐÃ NGẮT: Lỗi khi gọi Product Service. Không thể tạo đơn hàng. Chi tiết: {}", ex.getMessage());

        // Trả ra một Exception có chủ đích để Gateway/Controller bắt được và báo về Frontend
        throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
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