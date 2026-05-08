package vn.nbh.paymentservice.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.nbh.paymentservice.enums.PaymentStatus;
import java.math.BigDecimal;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends AuditModel { // Nhớ copy class AuditModel sang nhé

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(nullable = false)
    private BigDecimal amount;

    // Link URL để hiển thị mã QR cho Frontend
    @Column(name = "checkout_url", length = 1000)
    private String checkoutUrl;

    // Mã giao dịch lưu trên hệ thống PayOS để sau này đối soát
    @Column(name = "payment_link_id")
    private String paymentLinkId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;
}