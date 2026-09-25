package com.laptophub.product.service.impl;

import com.laptophub.brand.entity.Brand;
import com.laptophub.brand.enums.BrandStatus;
import com.laptophub.brand.service.BrandService;
import com.laptophub.category.entity.Category;
import com.laptophub.category.enums.CategoryStatus;
import com.laptophub.category.service.CategoryService;
import com.laptophub.product.dto.request.ProductCreateRequest;
import com.laptophub.product.dto.request.ProductUpdateRequest;
import com.laptophub.product.dto.response.ProductSummaryResponse;
import com.laptophub.product.entity.Product;
import com.laptophub.product.enums.ProductStatus;
import com.laptophub.product.repository.ProductRepository;
import com.laptophub.product.service.ProductCacheService;
import com.laptophub.product.service.ProductService;
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
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final BrandService brandService;
    private final ProductCacheService productCacheService;
    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryService categoryService, BrandService brandService,
                              ProductCacheService productCacheService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.brandService = brandService;
        this.productCacheService = productCacheService;
    }

    @Override
    @Transactional
    public Product create(ProductCreateRequest request) {
        Category category = requireActiveCategory(request.categoryId());
        Brand brand = requireActiveBrand(request.brandId());
        String slug = resolveSlug(request.slug(), request.name());
        if (productRepository.existsBySlug(slug)) {
            throw new AppException(ErrorCode.RESOURCE_CONFLICT, "Slug đã tồn tại");
        }
        return productRepository.save(Product.create(category.getId(), brand.getId(), request.name(), slug,
                request.shortDescription(), request.description()));
    }

    @Override
    @Transactional
    public Product update(Long id, ProductUpdateRequest request) {
        Product product = getByIdOrThrow(id);
        String oldSlug = product.getSlug();
        Category category = requireActiveCategory(request.categoryId());
        Brand brand = requireActiveBrand(request.brandId());
        String slug = resolveSlug(request.slug(), request.name());
        if (productRepository.existsBySlugAndIdNot(slug, id)) {
            throw new AppException(ErrorCode.RESOURCE_CONFLICT, "Slug đã tồn tại");
        }
        product.update(request.name(), slug, request.shortDescription(), request.description());
        product.changeCategory(category.getId());
        product.changeBrand(brand.getId());
        productCacheService.evictProductDetail(oldSlug);
        productCacheService.evictProductSearch();
        return product;
    }

    @Override
    public Product getByIdOrThrow(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }
    @Override
    public Page<ProductSummaryResponse> listAdmin(Long categoryId, Long brandId, ProductStatus status,
                                                  String keyword, Pageable pageable) {
        Page<Product> page = productRepository.searchAdmin(categoryId, brandId, status, keyword, pageable);
        Map<Long, String> categoryNames = categoryService.findNamesByIds(
                page.getContent().stream().map(Product::getCategoryId).distinct().toList());
        Map<Long, String> brandNames = brandService.findNamesByIds(
                page.getContent().stream().map(Product::getBrandId).distinct().toList());
        return page.map(p -> ProductSummaryResponse.from(p, categoryNames.get(p.getCategoryId()),
                brandNames.get(p.getBrandId())));
    }

    @Override
    @Transactional
    public Product activate(Long id) {
        Product product = getByIdOrThrow(id);
        product.activate();
        productCacheService.evictProductDetail(product.getSlug());
        productCacheService.evictProductSearch();
        return product;
    }

    @Override
    @Transactional
    public Product deactivate(Long id) {
        Product product = getByIdOrThrow(id);
        product.deactivate();
        productCacheService.evictProductDetail(product.getSlug());
        productCacheService.evictProductSearch();
        return product;
    }

    @Override
    public Map<Long, Product> findByIds(List<Long> ids) {
        return productRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
    }

    private Category requireActiveCategory(Long categoryId) {
        Category category = categoryService.getByIdOrThrow(categoryId);
        if (category.getStatus() != CategoryStatus.ACTIVE) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Danh mục đã ngừng hoạt động");
        }
        return category;
    }

    private Brand requireActiveBrand(Long brandId) {
        Brand brand = brandService.getById(brandId);
        if (brand.getStatus() != BrandStatus.ACTIVE) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Thương hiệu đã ngừng hoạt động");
        }
        return brand;
    }

    private String resolveSlug(String requestedSlug, String name) {
        String source = (requestedSlug == null || requestedSlug.isBlank()) ? name : requestedSlug;
        return SlugUtil.generate(source);
    }
}
