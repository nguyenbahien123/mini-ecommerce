package vn.nbh.notificationservice.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import vn.nbh.notificationservice.client.BrevoClient;
import vn.nbh.notificationservice.client.OrderClient;
import vn.nbh.notificationservice.client.UserClient;
import vn.nbh.notificationservice.dto.request.BrevoEmailRequest;
import vn.nbh.notificationservice.dto.response.OrderResponse;
import vn.nbh.notificationservice.dto.response.UserResponse;
import vn.nbh.notificationservice.event.PaymentSuccessEvent;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderClient orderClient;
    private final UserClient userClient;
    private final BrevoClient brevoClient;

    @Value("${brevo.api-key}")
    private String brevoApiKey;

    @Value("${brevo.sender-email}")
    private String senderEmail;

    @Value("${brevo.sender-name}")
    private String senderName;

    @KafkaListener(topics = "payment-success-topic", groupId = "notification-group")
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("Nhận tín hiệu Thanh toán thành công cho Đơn hàng ID: {}", event.getOrderId());

        try {
            // 1. Lấy thông tin Đơn hàng
            OrderResponse order = orderClient.getOrderById(event.getOrderId()).getResult();

            // 2. Lấy thông tin User (Lấy Email)
            UserResponse user = userClient.getUserById(order.getUserId()).getResult();

            // 3. Gửi Email thông qua Brevo
            sendSuccessEmail(user.getEmail(), user.getUsername(), order.getId());

            log.info("Đã gửi Email hóa đơn thành công cho khách hàng: {}", user.getEmail());

        } catch (Exception e) {
            log.error("Lỗi khi xử lý gửi Email cho Đơn hàng ID {}: {}", event.getOrderId(), e.getMessage());
        }
    }

    private void sendSuccessEmail(String customerEmail, String customerName, Long orderId) {
        BrevoEmailRequest.Sender sender = new BrevoEmailRequest.Sender(senderName, senderEmail);
        BrevoEmailRequest.To to = new BrevoEmailRequest.To(customerName, customerEmail);

        String htmlContent = String.format(
                "<html><body>" +
                        "<h2>Xin chào %s,</h2>" +
                        "<p>Cảm ơn bạn đã mua sắm tại <b>%s</b>.</p>" +
                        "<p>Đơn hàng <b>#%d</b> của bạn đã được thanh toán thành công và đang được chuẩn bị giao đến bạn.</p>" +
                        "<p>Trân trọng,</p>" +
                        "</body></html>",
                customerName, senderName, orderId
        );

        BrevoEmailRequest request = BrevoEmailRequest.builder()
                .sender(sender)
                .to(List.of(to))
                .subject("Xác nhận thanh toán thành công - Đơn hàng #" + orderId)
                .htmlContent(htmlContent)
                .build();

        brevoClient.sendEmail(brevoApiKey, request);
    }
}