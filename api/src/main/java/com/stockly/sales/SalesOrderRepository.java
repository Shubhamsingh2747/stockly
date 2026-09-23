package com.stockly.sales;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    @EntityGraph(attributePaths = {"lines", "lines.product", "createdBy"})
    List<SalesOrder> findAll();

    @EntityGraph(attributePaths = {"lines", "lines.product", "createdBy"})
    Optional<SalesOrder> findById(Long id);
}
