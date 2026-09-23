package com.stockly.category;

import com.stockly.category.dto.CategoryRequest;
import com.stockly.category.dto.CategoryResponse;
import com.stockly.common.config.CacheConfig;
import com.stockly.common.exception.ConflictException;
import com.stockly.common.exception.NotFoundException;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Cacheable(cacheNames = CacheConfig.CATEGORIES, key = "'all'")
    @Transactional(readOnly = true)
    public List<CategoryResponse> list() {
        return categoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Category getEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found: " + id));
    }

    @CacheEvict(cacheNames = {CacheConfig.CATEGORIES, CacheConfig.PRODUCTS, CacheConfig.PRODUCT_LISTS}, allEntries = true)
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("Category already exists");
        }
        Category category = new Category();
        category.setName(request.name().trim());
        return toResponse(categoryRepository.save(category));
    }

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}
