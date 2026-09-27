package com.stockly.product;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    boolean existsBySkuIgnoreCase(String sku);

    long countByCategoryId(Long categoryId);

    boolean existsByCategoryId(Long categoryId);

    @Query("select count(p) from Product p where p.stockQuantity <= p.reorderLevel")
    long countLowStock();

    @EntityGraph(attributePaths = "category")
    @Query("select p from Product p where p.stockQuantity <= p.reorderLevel order by p.sku")
    List<Product> findLowStock();
}
