package vn.nbh.orderservice.service;

import vn.nbh.orderservice.dto.request.OrderRequest;
import vn.nbh.orderservice.dto.response.OrderResponse;

public interface OrderService {
     OrderResponse createOrder(OrderRequest orderRequest);
     void  cancelOrder(Long orderId, String reason);
     void cancelOrderDueToPaymentFailure(Long orderId, String reason);
     void confirmOrder(Long orderId);
}
