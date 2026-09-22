package com.laptophub.category.service.impl;

import com.laptophub.category.dto.request.CategoryCreateRequest;
import com.laptophub.category.dto.request.CategoryUpdateRequest;
import com.laptophub.category.entity.Category;
import com.laptophub.category.enums.CategoryStatus;
import com.laptophub.category.repository.CategoryRepository;
import com.laptophub.category.service.CategoryService;
import com.laptophub.product.repository.ProductRepository;
import com.laptophub.product.service.ProductCacheService;
import com.laptophub.shared.exception.AppException;
import com.laptophub.shared.exception.ErrorCode;
import com.laptophub.shared.util.SlugUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductCacheService productCacheService;
    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               ProductCacheService productCacheService) {
        this.categoryRepository = categoryRepository;
        this.productCacheService = productCacheService;
    }

    @Override
    @Transactional
    public Category create(CategoryCreateRequest request) {
        String slug = resolveSlug(request.slug(), request.name());
        if (categoryRepository.existsBySlug(slug)) {
            throw new AppException(ErrorCode.RESOURCE_CONFLICT, "Slug đã tồn tại");
        }
        return categoryRepository.save(Category.create(request.name(), slug, request.description()));
    }
    @Override
    @Transactional
    public Category update(Long id, CategoryUpdateRequest request) {
        Category category = getByIdOrThrow(id);
        String slug = resolveSlug(request.slug(), request.name());
        if (categoryRepository.existsBySlugAndIdNot(slug, id)) {
            throw new AppException(ErrorCode.RESOURCE_CONFLICT, "Slug đã tồn tại");
        }
        category.update(request.name(), slug, request.description());
        productCacheService.evictAllProductDetail();
        productCacheService.evictProductSearch();
        return category;
    }

    @Override
    public Category getByIdOrThrow(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    public Page<Category> list(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Override
    public List<Category> listActive() {
        return categoryRepository.findByStatusOrderByNameAsc(CategoryStatus.ACTIVE);
    }

    @Override
    @Transactional
    public Category activate(Long id) {
        Category category = getByIdOrThrow(id);
        category.activate();
        productCacheService.evictAllProductDetail();
        productCacheService.evictProductSearch();
        return category;
    }

    @Override
    @Transactional
    public Category deactivate(Long id) {
        Category category = getByIdOrThrow(id);
        category.deactivate();
        productCacheService.evictAllProductDetail();
        productCacheService.evictProductSearch();
        return category;
    }

    @Override
    public Map<Long, String> findNamesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return categoryRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
    }

    private String resolveSlug(String requestedSlug, String name) {
        String source = (requestedSlug == null || requestedSlug.isBlank()) ? name : requestedSlug;
        return SlugUtil.generate(source);
    }
}
