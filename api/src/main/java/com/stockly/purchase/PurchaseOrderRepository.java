package com.stockly.purchase;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @EntityGraph(attributePaths = {"lines", "lines.product", "createdBy"})
    List<PurchaseOrder> findAll();

    @EntityGraph(attributePaths = {"lines", "lines.product", "createdBy"})
    Optional<PurchaseOrder> findById(Long id);
}
