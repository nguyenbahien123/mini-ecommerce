package vn.nbh.userservice.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.nbh.userservice.entity.InvalidatedToken;

import java.util.Date;

@Repository
public interface InvalidTokenRepository extends JpaRepository<InvalidatedToken,String> {
    @Transactional
    void deleteAllByExpiryTimeBefore(Date now);
}
