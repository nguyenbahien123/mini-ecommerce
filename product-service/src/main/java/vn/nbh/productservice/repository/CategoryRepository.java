package vn.nbh.productservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.nbh.productservice.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
     boolean existsByName(String name);
}
