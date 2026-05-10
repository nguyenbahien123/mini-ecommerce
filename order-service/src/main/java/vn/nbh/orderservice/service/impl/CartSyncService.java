package vn.nbh.orderservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.nbh.orderservice.entity.CartItem;
import vn.nbh.orderservice.repository.CartItemRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartSyncService {

    private final CartItemRepository cartItemRepository;

    // Chỉ định đích danh Thread Pool vừa tạo ở Bước 1
    @Async("cartAsyncExecutor")
    @Transactional
    public void syncAddToCartToDb(Integer userId, Long productId, Integer quantity) {
        log.info("[Async DB Sync] Đang đồng bộ thêm sản phẩm {} cho user {}", productId, userId);

        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElse(CartItem.builder()
                        .userId(userId)
                        .productId(productId)
                        .quantity(0) // Mặc định 0 nếu là thêm mới
                        .build());

        item.setQuantity(item.getQuantity() + quantity);
        cartItemRepository.save(item);
    }

    @Async("cartAsyncExecutor")
    @Transactional
    public void syncRemoveFromDb(Integer userId, Long productId) {
        log.info("[Async DB Sync] Đang đồng bộ xóa sản phẩm {} cho user {}", productId, userId);
        cartItemRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Async("cartAsyncExecutor")
    @Transactional
    public void syncClearCartInDb(Integer userId) {
        log.info("[Async DB Sync] Đang đồng bộ dọn sạch giỏ hàng cho user {}", userId);
        cartItemRepository.deleteByUserId(userId);
    }
}