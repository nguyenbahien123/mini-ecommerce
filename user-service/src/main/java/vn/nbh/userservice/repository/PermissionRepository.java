package vn.nbh.userservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.nbh.userservice.entity.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {
    @Query("""
    SELECT u FROM Permission u 
    WHERE u.isActive = true AND (
        u.createdBy LIKE :keyword OR
        u.updatedBy LIKE :keyword OR
        u.name LIKE :keyword OR
        u.description LIKE :keyword
    )
""")
    Page<Permission> searchByKeyword(String keyword, Pageable pageable);
}
