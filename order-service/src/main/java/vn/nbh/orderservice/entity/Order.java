package vn.nbh.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.nbh.orderservice.enums.OrderStatus;
import vn.nbh.orderservice.enums.PaymentMethod;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "orders")
public class Order extends AuditModel{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    // Lưu userId từ JWT token, KHÔNG @ManyToOne vì bảng User nằm ở service khác
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "shipping_address", nullable = false)
    private String shippingAddress;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    // CascadeType.ALL: Khi save Order sẽ save luôn các OrderDetail bên trong nó
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderDetail> orderDetails = new ArrayList<>();

    // Hàm tiện ích để add detail chuẩn xác 2 chiều
    public void addOrderDetail(OrderDetail detail) {
        orderDetails.add(detail);
        detail.setOrder(this);
    }
}
