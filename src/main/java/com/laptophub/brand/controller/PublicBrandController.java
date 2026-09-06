package com.laptophub.brand.controller;

import com.laptophub.brand.dto.response.BrandPublicResponse;
import com.laptophub.brand.service.BrandService;
import com.laptophub.shared.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/brands")
public class PublicBrandController {

    private final BrandService brandService;

    public PublicBrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BrandPublicResponse>>> list() {

        List<BrandPublicResponse> responses =
                brandService.listActive()
                        .stream()
                        .map(BrandPublicResponse::from)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success("oke",responses)
        );
    }
}