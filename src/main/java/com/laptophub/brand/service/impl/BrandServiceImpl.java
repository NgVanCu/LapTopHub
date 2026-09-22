package com.laptophub.brand.service.impl;

import com.laptophub.brand.dto.request.BrandCreateRequest;
import com.laptophub.brand.dto.request.BrandUpdateRequest;
import com.laptophub.brand.dto.response.BrandResponse;
import com.laptophub.brand.entity.Brand;
import com.laptophub.brand.enums.BrandStatus;
import com.laptophub.brand.repository.BrandRepository;
import com.laptophub.brand.service.BrandService;
import com.laptophub.product.service.ProductCacheService;
import com.laptophub.shared.exception.AppException;
import com.laptophub.shared.exception.ErrorCode;
import com.laptophub.shared.util.SlugUtil;
import com.laptophub.storage.dto.request.ConfirmUploadRequest;
import com.laptophub.storage.enums.ImagePurpose;
import com.laptophub.storage.service.ImageStorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final ImageStorageService imageStorageService;
    private final ProductCacheService productCacheService;
    public BrandServiceImpl(
            BrandRepository brandRepository,
            ImageStorageService imageStorageService,
            ProductCacheService productCacheService
    ) {
        this.brandRepository = brandRepository;
        this.imageStorageService = imageStorageService;
        this.productCacheService = productCacheService;
    }

    @Override
    @Transactional
    public BrandResponse create(BrandCreateRequest request) {

        String slug = resolveSlug(
                request.slug(),
                request.name()
        );

        if (brandRepository.existsBySlug(slug)) {
            throw new AppException(ErrorCode.RESOURCE_CONFLICT, "Slug đã tồn tại");
        }

        // 1. Tạo Brand trước để có brandId
        Brand brand = Brand.create(
                request.name(),
                slug,
                request.description(),
                null
        );

        brandRepository.save(brand);

        // 2. Nếu frontend đã upload logo vào MinIO
        if (request.logoKey() != null
                && !request.logoKey().isBlank()) {

            String finalLogoKey =
                    imageStorageService.confirmUpload(
                            ImagePurpose.BRAND_LOGO,
                            brand.getId(),
                            new ConfirmUploadRequest(
                                    request.logoKey()
                            )
                    );

            // 3. Lưu permanent key vào Brand
            brand.changeLogo(finalLogoKey);
        }

        return toResponse(brand);
    }

    @Override
    @Transactional
    public BrandResponse update(
            Long id,
            BrandUpdateRequest request
    ) {
        Brand brand = getEntityByIdOrThrow(id);

        String slug = resolveSlug(
                request.slug(),
                request.name()
        );

        if (brandRepository.existsBySlugAndIdNot(slug, id)) {
            throw new AppException(ErrorCode.RESOURCE_CONFLICT, "Slug đã tồn tại");
        }

        String oldLogoKey = brand.getLogoKey();
        String newLogoKey = oldLogoKey;

        if (request.logoKey() != null
                && !request.logoKey().isBlank()
                && !request.logoKey().equals(oldLogoKey)) {

            newLogoKey = imageStorageService.confirmUpload(
                    ImagePurpose.BRAND_LOGO,
                    brand.getId(),
                    new ConfirmUploadRequest(request.logoKey())
            );
        }

        brand.update(
                request.name(),
                slug,
                request.description(),
                newLogoKey
        );
        productCacheService.evictAllProductDetail();
        productCacheService.evictProductSearch();
        return toResponse(brand);
    }

    @Override
    public BrandResponse getByIdOrThrow(Long id) {

        Brand brand = getEntityByIdOrThrow(id);

        return toResponse(brand);
    }

    @Override
    public Page<BrandResponse> list(Pageable pageable) {

        Page<Brand> brandPage =
                brandRepository.findAll(pageable);

        return brandPage.map(this::toResponse);
    }

    @Override
    public List<BrandResponse> listActive() {

        return brandRepository
                .findByStatusOrderByNameAsc(BrandStatus.ACTIVE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public BrandResponse activate(Long id) {

        Brand brand = getEntityByIdOrThrow(id);

        brand.activate();
        productCacheService.evictAllProductDetail();
        productCacheService.evictProductSearch();
        return toResponse(brand);
    }

    @Override
    @Transactional
    public BrandResponse deactivate(Long id) {

        Brand brand = getEntityByIdOrThrow(id);

        brand.deactivate();
        productCacheService.evictAllProductDetail();
        productCacheService.evictProductSearch();
        return toResponse(brand);
    }

    @Override
    public Map<Long, String> findNamesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return brandRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Brand::getId, Brand::getName));
    }

    @Override
    public Brand getById(Long id){
        return getEntityByIdOrThrow(id);
    }
    /**
     * Lấy Brand Entity từ database.
     *
     * Method này chỉ dùng bên trong BrandServiceImpl.
     */
    private Brand getEntityByIdOrThrow(Long id) {

        return brandRepository.findById(id)
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.RESOURCE_NOT_FOUND
                        )
                );
    }

    /**
     * Tạo Presigned GET URL từ logoKey
     * để trả về cho frontend.
     */
    private BrandResponse toResponse(Brand brand) {

        String logoUrl = null;

        if (brand.getLogoKey() != null
                && !brand.getLogoKey().isBlank()) {

            logoUrl =
                    imageStorageService.generateDownloadUrl(
                            brand.getLogoKey()
                    );
        }

        return BrandResponse.from(
                brand,
                logoUrl
        );
    }

    private String resolveSlug(
            String requestedSlug,
            String name
    ) {

        String source =
                (requestedSlug == null
                        || requestedSlug.isBlank())
                        ? name
                        : requestedSlug;

        return SlugUtil.generate(source);
    }
}