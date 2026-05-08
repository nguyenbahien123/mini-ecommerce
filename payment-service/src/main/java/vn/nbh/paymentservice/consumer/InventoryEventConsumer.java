package vn.nbh.paymentservice.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import vn.nbh.paymentservice.entity.Payment;
import vn.nbh.paymentservice.enums.PaymentStatus;
import vn.nbh.paymentservice.event.InventoryDeductedEvent;
import vn.nbh.paymentservice.event.PaymentFailedEvent;
import vn.nbh.paymentservice.repository.PaymentRepository;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.LinkedHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class InventoryEventConsumer {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    @Value("${payos.return-url}")
    private String returnUrl;

    @Value("${payos.cancel-url}")
    private String cancelUrl;

    private String createPaymentRequestSignature(int amount, String cancelUrl, String description, long orderCode, String returnUrl) throws Exception {
        String data = "amount=" + amount
                + "&cancelUrl=" + cancelUrl
                + "&description=" + description
                + "&orderCode=" + orderCode
                + "&returnUrl=" + returnUrl;

        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        mac.init(new javax.crypto.spec.SecretKeySpec(checksumKey.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] result = mac.doFinal(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder(result.length * 2);
        for (byte b : result) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @KafkaListener(topics = "inventory-deducted-topic", groupId = "payment-group")
    public void handleInventoryDeducted(InventoryDeductedEvent event) {
        log.info("Nhận yêu cầu tạo thanh toán cho Đơn hàng ID: {}", event.getOrderId());

        try {
            // 1. Tạo request gửi sang PayOS
            // OrderCode của PayOS yêu cầu là số nguyên dài max 16 số và phải DUY NHẤT. Do dùng lại API key cũ nên có thể trùng orderCode cũ.
            String timeSuffix = String.valueOf(System.currentTimeMillis());
            timeSuffix = timeSuffix.substring(timeSuffix.length() - 5);
            long orderCode = Long.parseLong(event.getOrderId() + timeSuffix);
            int amount = event.getAmountToPay().intValue();


            String description = "";
            if (description.length() > 25) {
                description = description.substring(0, 25);
            }

            // 2. Gọi API PayOS trực tiếp thay vì SDK do SDK bị lỗi deserialize unknown props (expiredAt)
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-client-id", clientId);
            headers.set("x-api-key", apiKey);
            headers.set("Content-Type", "application/json");

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("orderCode", orderCode);
            body.put("amount", amount);
            body.put("description", description);
            body.put("cancelUrl", cancelUrl);
            body.put("returnUrl", returnUrl);
            body.put("signature", createPaymentRequestSignature(amount, cancelUrl, description, orderCode, returnUrl));

            Map<String, Object> itemMap = new LinkedHashMap<>();
            itemMap.put("name", "Thanh toán Đơn hàng #" + event.getOrderId());
            itemMap.put("price", amount);
            itemMap.put("quantity", 1);
            body.put("items", java.util.Collections.singletonList(itemMap));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> responseEntity = restTemplate.exchange("https://api-merchant.payos.vn/v2/payment-requests", HttpMethod.POST, request, Map.class);
            Map<String, Object> responseBody = responseEntity.getBody();

            if (responseBody == null || !"00".equals(responseBody.get("code"))) {
                throw new RuntimeException("PayOS trả về lỗi: " + (responseBody != null ? responseBody.get("desc") : "Unknown"));
            }

            Map<String, Object> responseData = (Map<String, Object>) responseBody.get("data");
            String checkoutUrlStr = (String) responseData.get("checkoutUrl");
            String paymentLinkIdStr = (String) responseData.get("paymentLinkId");

            // 3. Lưu vào Database
            Payment payment = Payment.builder()
                    .orderId(event.getOrderId())
                    .amount(event.getAmountToPay())
                    .checkoutUrl(checkoutUrlStr)
                    .paymentLinkId(paymentLinkIdStr)
                    .status(PaymentStatus.PENDING)
                    .build();
            paymentRepository.save(payment);

            log.info("Tạo link PayOS thành công cho Đơn hàng ID: {}", event.getOrderId());
            log.info("Vui lòng truy cập link sau để thanh toán: {}", checkoutUrlStr);

            // TODO: Bạn có thể bắn 1 Event "payment-link-created-topic" về cho OrderService
            // để cập nhật link QR vào database đơn hàng cho User dễ lấy.

        } catch (Exception e) {
            log.error("Lỗi khi tạo thanh toán PayOS: {}", e.getMessage(), e);

            // ROLLBACK SAGA: Nếu PayOS sập, báo cho Order và Product biết để Hủy đơn & Cộng lại kho!
            PaymentFailedEvent failedEvent = new PaymentFailedEvent(event.getOrderId(), "Lỗi tạo cổng thanh toán");
            kafkaTemplate.send("payment-failed-topic", String.valueOf(event.getOrderId()), failedEvent);
        }
    }
}