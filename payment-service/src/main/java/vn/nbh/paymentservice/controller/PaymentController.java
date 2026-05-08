package vn.nbh.paymentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import vn.nbh.paymentservice.entity.Payment;
import vn.nbh.paymentservice.enums.PaymentStatus;
import vn.nbh.paymentservice.event.PaymentSuccessEvent;
import vn.nbh.paymentservice.repository.PaymentRepository;

import org.springframework.beans.factory.annotation.Value;
import java.util.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tags
public class PaymentController {

    @Value("${payos.checksum-key}")
    private String checksumKey;

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final ObjectMapper objectMapper;

    private String hmacSha256(String key, String data) throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        mac.init(new javax.crypto.spec.SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(result.length * 2);
        for (byte b : result) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(Optional.ofNullable(value).orElse(""), StandardCharsets.UTF_8);
    }

    private Object normalizeNested(Object valueObj) {
        if (valueObj instanceof Map<?, ?> map) {
            Map<String, Object> sorted = new TreeMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                sorted.put(String.valueOf(entry.getKey()), normalizeNested(entry.getValue()));
            }
            return sorted;
        }
        if (valueObj instanceof Collection<?> col) {
            List<Object> list = new ArrayList<>();
            for (Object item : col) {
                list.add(normalizeNested(item));
            }
            return list;
        }
        return valueObj;
    }

    private String generateSignatureFromData(Map<String, Object> data) throws Exception {
        if (data == null || data.isEmpty()) {
            return "";
        }
        List<String> keys = new ArrayList<>(data.keySet());
        Collections.sort(keys);

        List<String> pairs = new ArrayList<>();
        for (String key : keys) {
            Object valueObj = data.get(key);
            String value;
            if (valueObj == null || "null".equalsIgnoreCase(String.valueOf(valueObj)) || "undefined".equalsIgnoreCase(String.valueOf(valueObj))) {
                value = "";
            } else if (valueObj instanceof Map || valueObj instanceof Collection) {
                Object normalized = normalizeNested(valueObj);
                value = objectMapper.writeValueAsString(normalized);
            } else {
                value = String.valueOf(valueObj);
            }
            pairs.add(key + "=" + urlEncode(value));
        }

        String dataStr = String.join("&", pairs);
        return hmacSha256(checksumKey, dataStr);
    }

    public boolean verifyWebhookSignature(Map<String, Object> payload) throws Exception {
        if (payload == null) {
            return false;
        }
        Object dataObj = payload.get("data");
        Object signatureObj = payload.get("signature");
        if (dataObj == null || signatureObj == null) {
            return false;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> data = objectMapper.convertValue(dataObj, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        String signature = String.valueOf(signatureObj);
        String computed = generateSignatureFromData(data);
        boolean match = signature.equalsIgnoreCase(computed);
        return match;
    }

    @Operation(summary = "Xử lý Webhook từ PayOS sau khi khách hàng thanh toán")
    @PostMapping("/webhook")
    public ObjectNode handlePayOSWebhook(@RequestBody ObjectNode webhookBody) {
        log.info("Nhận Webhook từ PayOS: {}", webhookBody.toString());

        try {
            Map<String, Object> payload = objectMapper.convertValue(webhookBody, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});

            if (!verifyWebhookSignature(payload)) {
                throw new RuntimeException("Chữ ký không hợp lệ");
            }

            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            String code = String.valueOf(data.get("code"));

            // 3. Kiểm tra nếu giao dịch thành công (00 = Thành công)
            if ("00".equals(code)) {
                Long orderCode = Long.valueOf(String.valueOf(data.get("orderCode")));

                // Tìm bằng mã orderCode hoặc bỏ timestamp suffix (suffix là 5 chars ở cuối)
                String orderCodeStr = String.valueOf(orderCode);
                Long realOrderId;
                if (orderCodeStr.length() > 5) {
                    realOrderId = Long.valueOf(orderCodeStr.substring(0, orderCodeStr.length() - 5));
                } else {
                    realOrderId = orderCode;
                }

                // 3. Cập nhật Database Payment
                Payment payment = paymentRepository.findByOrderId(realOrderId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));

                payment.setStatus(PaymentStatus.SUCCESS);
                paymentRepository.save(payment);

                // 4. CHỐT HẠ SAGA: Bắn event báo cho OrderService và NotificationService
                PaymentSuccessEvent successEvent = new PaymentSuccessEvent(realOrderId);
                kafkaTemplate.send("payment-success-topic", String.valueOf(realOrderId), successEvent);

                log.info("Xử lý thành công thanh toán cho Đơn hàng ID: {}", realOrderId);
            }

            // PayOS yêu cầu trả về object này để xác nhận đã nhận webhook
            return webhookBody.put("success", true);

        } catch (Exception e) {
            log.error("Lỗi bảo mật hoặc xử lý Webhook: {}", e.getMessage(), e);
            return webhookBody.put("success", false);
        }
    }
}