package vn.nbh.orderservice.service;

import java.util.Map;

public interface CartService {
    void addToCart(Integer userId, Long productId, Integer quantity);
    Map<Long, Integer> getCart(Integer userId);
    void removeFromCart(Integer userId, Long productId);
    void clearCart(Integer userId);
}
