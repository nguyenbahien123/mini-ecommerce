package vn.nbh.orderservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import vn.nbh.orderservice.entity.CartItem;
import vn.nbh.orderservice.repository.CartItemRepository;
import vn.nbh.orderservice.service.CartService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CartSyncService cartSyncService;
    private final CartItemRepository cartItemRepository;

    // Prefix cho key trong Redis
    private static final String CART_PREFIX = "cart:";
    // Giỏ hàng tự động bị xóa sau 7 ngày nếu không có tương tác
    private static final long CART_TTL_DAYS = 7;

    @Override
    public void addToCart(Integer userId, Long productId, Integer quantity) {
        String key = CART_PREFIX + userId;
        HashOperations<String, String, Integer> hashOps = redisTemplate.opsForHash();

        // Kiểm tra xem sản phẩm đã có trong giỏ chưa
        String hashKey = String.valueOf(productId);
        Integer currentQty = hashOps.get(key, hashKey);

        if (currentQty != null) {
            // Nếu có rồi thì cộng dồn số lượng
            hashOps.put(key, hashKey, currentQty + quantity);
        } else {
            // Chưa có thì thêm mới
            hashOps.put(key, hashKey, quantity);
        }

        // Gia hạn thời gian sống của giỏ hàng thêm 7 ngày kể từ lần tương tác cuối
        redisTemplate.expire(key, CART_TTL_DAYS, TimeUnit.DAYS);
        log.info("Đã cập nhật giỏ hàng cho User {}: Sản phẩm {} - Số lượng cộng thêm {}", userId, productId, quantity);

        // Kích hoạt luồng Async lưu xuống MySQL (Chạy ngầm, không block FE)
        cartSyncService.syncAddToCartToDb(userId, productId, quantity);
    }

    @Override
    public Map<Long, Integer> getCart(Integer userId) {
        String key = CART_PREFIX + userId;

        Map<Object, Object> rawCart =
                redisTemplate.opsForHash().entries(key);

        // 1. Trúng Cache (Cache Hit): Redis có dữ liệu
        if (!rawCart.isEmpty()) {

            return rawCart.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            e -> Long.parseLong(e.getKey().toString()),
                            e -> Integer.parseInt(e.getValue().toString())
                    ));
        }


        // 2. Trượt Cache (Cache Miss): Chui xuống MySQL tìm
        List<CartItem> dbCart =
                cartItemRepository.findByUserId(userId);

        if (dbCart.isEmpty()) {
            return Collections.emptyMap();
        }

        // 3. Khôi phục lại Redis từ MySQL
        Map<Long, Integer> result = dbCart.stream()
                .collect(Collectors.toMap(
                        CartItem::getProductId,
                        CartItem::getQuantity
                ));
        Map<String, Integer> redisMap = dbCart.stream()
                .collect(Collectors.toMap(
                        item -> item.getProductId().toString(),
                        CartItem::getQuantity
                ));

        redisTemplate.opsForHash().putAll(key, redisMap);

        redisTemplate.expire(key, CART_TTL_DAYS, TimeUnit.DAYS); // Set lại TTL 7 ngày

        return result;
    }

    @Override
    public void removeFromCart(Integer userId, Long productId) {
        String key = CART_PREFIX + userId;
        redisTemplate.opsForHash().delete(key, String.valueOf(productId));
        log.info("Đã xóa sản phẩm {} khỏi giỏ hàng của User {}", productId, userId);
        // Kích hoạt luồng Async xóa khỏi MySQL (Chạy ngầm, không block FE)
        cartSyncService.syncRemoveFromDb(userId, productId);
    }

    @Override
    public void clearCart(Integer userId) {
        String key = CART_PREFIX + userId;
        redisTemplate.delete(key);
        log.info("Đã dọn sạch giỏ hàng của User {}", userId);

        // Kích hoạt luồng Async xóa sạch MySQL (Chạy ngầm, không block FE)
        cartSyncService.syncClearCartInDb(userId);
    }
}