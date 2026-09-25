package com.laptophub.cart.controller;

import com.laptophub.cart.dto.request.CartItemAddRequest;
import com.laptophub.cart.dto.request.CartItemUpdateRequest;
import com.laptophub.cart.dto.response.CartItemResponse;
import com.laptophub.cart.dto.response.CartLine;
import com.laptophub.cart.dto.response.CartResponse;
import com.laptophub.cart.service.CartService;
import com.laptophub.product.entity.Product;
import com.laptophub.product.service.ProductImageService;
import com.laptophub.product.service.ProductService;
import com.laptophub.security.currentuser.CurrentUserProvider;
import com.laptophub.shared.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customer/cart")
public class CustomerCartController {
    private final CartService cartService;
    private final ProductService productService;
    private final ProductImageService productImageService;
    private final CurrentUserProvider currentUserProvider;

    public CustomerCartController(CartService cartService, ProductService productService,
                                  ProductImageService productImageService, CurrentUserProvider currentUserProvider) {
        this.cartService = cartService;
        this.productService = productService;
        this.productImageService = productImageService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> view() {
        Long userId = currentUserProvider.getCurrentUser().userId();
        List<CartLine> lines = cartService.getItemsWithLivePrice(userId);
        return ResponseEntity.ok(ApiResponse.success("Lấy giỏ hàng thành công",CartResponse.from(enrich(lines))));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartItemResponse>> addItem(@Valid @RequestBody CartItemAddRequest request) {
        Long userId = currentUserProvider.getCurrentUser().userId();
        var line = cartService.addItem(userId, request.productVariantId(), request.quantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Thêm sản phẩm vào giỏ hàng thành công",enrich(List.of(line)).getFirst()));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateQuantity(@PathVariable Long itemId,
                                                                        @Valid @RequestBody CartItemUpdateRequest request) {
        Long userId = currentUserProvider.getCurrentUser().userId();
        var line = cartService.updateQuantity(userId, itemId, request.quantity());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật số lượng sản phẩm thành công",enrich(List.of(line)).getFirst()));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long itemId) {
        Long userId = currentUserProvider.getCurrentUser().userId();
        cartService.removeItem(userId, itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clear() {
        Long userId = currentUserProvider.getCurrentUser().userId();
        cartService.clear(userId);
        return ResponseEntity.noContent().build();
    }

    // productName/thumbnailUrl không nằm trên CartItem/ProductVariant — batch
    // fetch Product + ảnh đại diện theo productId 1 lần cho cả danh sách,
    // tránh N+1 (đúng pattern ProductComparisonService).
    private List<CartItemResponse> enrich(List<CartLine> lines) {
        List<Long> productIds = lines.stream().map(line -> line.variant().getProductId()).distinct().toList();
        Map<Long, Product> productsById = productService.findByIds(productIds);
        Map<Long, String> thumbnailsByProductId = productImageService.findThumbnailUrlsByProductIds(productIds);
        return lines.stream()
                .map(line -> {
                    Product product = productsById.get(line.variant().getProductId());
                    return CartItemResponse.from(line, product.getName(), thumbnailsByProductId.get(product.getId()));
                })
                .toList();
    }
}
