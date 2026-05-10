package vn.nbh.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.nbh.orderservice.entity.CartItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserId(Integer userId);
    Optional<CartItem> findByUserIdAndProductId(Integer userId, Long productId);
    void deleteByUserIdAndProductId(Integer userId, Long productId);
    void deleteByUserId(Integer userId);
}
