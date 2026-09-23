package com.stockly.product;

import org.springframework.data.jpa.domain.Specification;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> skuContains(String sku) {
        return (root, query, cb) -> sku == null || sku.isBlank()
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("sku")), "%" + sku.toLowerCase() + "%");
    }

    public static Specification<Product> categoryIdEquals(Long categoryId) {
        return (root, query, cb) -> categoryId == null
                ? cb.conjunction()
                : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Product> lowStockOnly(Boolean lowStock) {
        return (root, query, cb) -> {
            if (lowStock == null || !lowStock) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("stockQuantity"), root.get("reorderLevel"));
        };
    }
}
