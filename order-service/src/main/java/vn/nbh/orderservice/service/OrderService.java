package vn.nbh.orderservice.service;

import vn.nbh.orderservice.dto.request.OrderRequest;
import vn.nbh.orderservice.dto.response.OrderResponse;

public interface OrderService {
    public OrderResponse createOrder(OrderRequest orderRequest);
}
