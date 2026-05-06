package vn.nbh.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetail extends AuditModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Lưu productId, KHÔNG @ManyToOne vì bảng Product nằm ở product-service
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    // Giá của 1 sản phẩm TẠI THỜI ĐIỂM MUA HÀNG
    @Column(nullable = false)
    private BigDecimal price;

    // Tổng tiền của dòng này (= quantity * price)
    @Column(name = "sub_total", nullable = false)
    private BigDecimal subTotal;
}
