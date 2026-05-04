package vn.nbh.userservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.nbh.userservice.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {


    @Query("""
    SELECT u FROM User u
    WHERE u.isActive = true AND (
        u.username LIKE :keyword OR
        u.email LIKE :keyword OR
        u.address LIKE :keyword OR
        u.phoneNumber LIKE :keyword
    )
""")
    Page<User> searchByKeyword(String keyword, Pageable pageable);

    Boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
}
