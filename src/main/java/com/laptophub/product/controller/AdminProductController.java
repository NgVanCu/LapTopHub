package com.laptophub.product.controller;

import com.laptophub.brand.service.BrandService;
import com.laptophub.category.service.CategoryService;
import com.laptophub.product.dto.request.*;
import com.laptophub.product.dto.response.*;
import com.laptophub.product.entity.Product;
import com.laptophub.product.entity.ProductVariant;
import com.laptophub.product.enums.ProductStatus;
import com.laptophub.product.service.ProductImageService;
import com.laptophub.product.service.ProductService;
import com.laptophub.product.service.ProductSpecValueService;
import com.laptophub.product.service.ProductVariantService;
import com.laptophub.shared.response.ApiResponse;
import com.laptophub.shared.response.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/products")
public class AdminProductController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandService brandService;
    private final ProductVariantService productVariantService;
    private final ProductImageService productImageService;
    private final ProductSpecValueService productSpecValueService;


    public AdminProductController(ProductService productService, CategoryService categoryService,
                                  BrandService brandService, ProductVariantService productVariantService,
                                  ProductImageService productImageService,
                                  ProductSpecValueService productSpecValueService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.brandService = brandService;
        this.productVariantService = productVariantService;
        this.productImageService = productImageService;
        this.productSpecValueService = productSpecValueService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody ProductCreateRequest request) {
        Product product = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo sản phẩm thành công!",toResponse(product)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> update(@PathVariable Long id,
                                                               @Valid @RequestBody ProductUpdateRequest request) {
        Product product = productService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật sản phẩm thành công!",toResponse(product)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductSummaryResponse>>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim().toLowerCase();
        var page = productService.listAdmin(categoryId, brandId, status, normalizedKeyword, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy sản phẩm thành công!",PageResponse.of(page)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getOne(@PathVariable Long id) {
        Product product = productService.getByIdOrThrow(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy sản phẩm thành công!",toResponse(product)));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<ProductResponse>> activate(@PathVariable Long id) {
        Product product = productService.activate(id);
        return ResponseEntity.ok(ApiResponse.success("Kích hoạt sản phẩm thành công!",toResponse(product)));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<ProductResponse>> deactivate(@PathVariable Long id) {
        Product product = productService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Ẩn sản phẩm thành công!",toResponse(product)));
    }

    @GetMapping("/{id}/variants")
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> listVariants(@PathVariable Long id) {
        var responses = productVariantService.listByProduct(id).stream().map(ProductVariantResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách biến thể sản phẩm thành công!",responses));
    }

    @PostMapping("/{id}/variants")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> addVariant(
            @PathVariable Long id, @Valid @RequestBody ProductVariantCreateRequest request) {
        ProductVariant variant = productVariantService.addVariant(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo biến thể sản phẩm thành công!",
                        ProductVariantResponse.from(variant)));
    }

    @PutMapping("/{id}/variants/{variantId}")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateVariant(
            @PathVariable Long id, @PathVariable Long variantId,
            @Valid @RequestBody ProductVariantUpdateRequest request) {
        ProductVariant variant = productVariantService.updateVariant(id, variantId, request);
        return ResponseEntity.ok(ApiResponse.success( "Cập nhật biến thể sản phẩm thành công!"
                ,ProductVariantResponse.from(variant)));
    }

    @PostMapping("/{id}/variants/{variantId}/activate")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> activateVariant(
            @PathVariable Long id, @PathVariable Long variantId) {
        ProductVariant variant = productVariantService.activate(id, variantId);
        return ResponseEntity.ok(ApiResponse.success("Kích hoạt biến thể sản phẩm thành công!"
                ,ProductVariantResponse.from(variant)));
    }

    @PostMapping("/{id}/variants/{variantId}/deactivate")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> deactivateVariant(
            @PathVariable Long id, @PathVariable Long variantId) {
        ProductVariant variant = productVariantService.deactivate(id, variantId);
        return ResponseEntity.ok(ApiResponse.success("Ẩn biến thể sản phẩm thành công!",
                ProductVariantResponse.from(variant)));
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> listImages(
            @PathVariable Long id
    ) {
        List<ProductImageResponse> responses =
                productImageService.listByProduct(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy danh sách ảnh thành công!",
                        responses
                )
        );
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<ApiResponse<ProductImageResponse>> addImage(
            @PathVariable Long id,
            @Valid @RequestBody ProductImageCreateRequest request
    ) {
        ProductImageResponse response =
                productImageService.addImage(
                        id,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Thêm ảnh sản phẩm thành công!",
                                response
                        )
                );
    }

    @PostMapping("/{id}/images/batch")
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> addImages(
            @PathVariable Long id,
            @Valid @RequestBody ProductImagesCreateRequest request
    ) {
        List<ProductImageResponse> responses =
                productImageService.addImages(
                        id,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Thêm ảnh sản phẩm thành công!",
                                responses
                        )
                );
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long id,
            @PathVariable Long imageId
    ) {
        productImageService.deleteImage(
                id,
                imageId
        );

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/images/reorder")
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> reorderImages(
            @PathVariable Long id,
            @Valid @RequestBody ProductImageReorderRequest request
    ) {
        List<ProductImageResponse> responses =
                productImageService.reorderImages(
                        id,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cập nhật thứ tự ảnh thành công!",
                        responses
                )
        );
    }

    @GetMapping("/{id}/specifications")
    public ResponseEntity<ApiResponse<List<ProductSpecValueResponse>>> listSpecifications(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("",productSpecValueService.listByProduct(id)));
    }

    @PutMapping("/{id}/specifications")
    public ResponseEntity<ApiResponse<List<ProductSpecValueResponse>>> upsertSpecifications(
            @PathVariable Long id, @Valid @RequestBody ProductSpecValuesUpsertRequest request) {
        var responses = productSpecValueService.upsertValues(id, request);
        return ResponseEntity.ok(ApiResponse.success("",responses));
    }

    private ProductResponse toResponse(Product product) {
        String categoryName = categoryService.getByIdOrThrow(product.getCategoryId()).getName();
        String brandName = brandService.getByIdOrThrow(product.getBrandId()).name();
        return ProductResponse.from(product, categoryName, brandName);
    }
}
