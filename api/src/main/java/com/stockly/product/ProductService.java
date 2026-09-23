package com.stockly.product;

import com.stockly.category.Category;
import com.stockly.category.CategoryService;
import com.stockly.common.config.CacheConfig;
import com.stockly.common.exception.ConflictException;
import com.stockly.common.exception.NotFoundException;
import com.stockly.product.dto.ProductRequest;
import com.stockly.product.dto.ProductResponse;
import java.time.Instant;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    @Cacheable(cacheNames = CacheConfig.PRODUCT_LISTS, key = "#sku + '-' + #categoryId + '-' + #lowStock")
    @Transactional(readOnly = true)
    public List<ProductResponse> list(String sku, Long categoryId, Boolean lowStock) {
        Specification<Product> spec = ProductSpecifications.skuContains(sku)
                .and(ProductSpecifications.categoryIdEquals(categoryId))
                .and(ProductSpecifications.lowStockOnly(lowStock));
        return productRepository.findAll(spec).stream().map(ProductMapper::toResponse).toList();
    }

    @Cacheable(cacheNames = CacheConfig.PRODUCTS, key = "#id")
    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return ProductMapper.toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public Product getEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
    }

    @CacheEvict(cacheNames = {CacheConfig.PRODUCTS, CacheConfig.PRODUCT_LISTS}, allEntries = true)
    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySkuIgnoreCase(request.sku())) {
            throw new ConflictException("SKU already exists");
        }
        Category category = categoryService.getEntity(request.categoryId());
        Product product = new Product();
        apply(product, request, category);
        return ProductMapper.toResponse(productRepository.save(product));
    }

    @CacheEvict(cacheNames = {CacheConfig.PRODUCTS, CacheConfig.PRODUCT_LISTS}, allEntries = true)
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getEntity(id);
        if (!product.getSku().equalsIgnoreCase(request.sku()) && productRepository.existsBySkuIgnoreCase(request.sku())) {
            throw new ConflictException("SKU already exists");
        }
        Category category = categoryService.getEntity(request.categoryId());
        apply(product, request, category);
        product.setUpdatedAt(Instant.now());
        return ProductMapper.toResponse(product);
    }

    private void apply(Product product, ProductRequest request, Category category) {
        product.setSku(request.sku().trim());
        product.setName(request.name().trim());
        product.setCategory(category);
        product.setUnitPrice(request.unitPrice());
        product.setStockQuantity(request.stockQuantity());
        product.setReorderLevel(request.reorderLevel());
        product.setUpdatedAt(Instant.now());
    }
}
