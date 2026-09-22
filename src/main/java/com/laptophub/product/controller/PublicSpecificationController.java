package com.laptophub.product.controller;

import com.laptophub.product.dto.response.SpecificationDefinitionResponse;
import com.laptophub.product.service.SpecificationDefinitionService;
import com.laptophub.shared.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/specifications")
public class PublicSpecificationController {

    private final SpecificationDefinitionService specificationDefinitionService;

    public PublicSpecificationController(
            SpecificationDefinitionService specificationDefinitionService
    ) {
        this.specificationDefinitionService = specificationDefinitionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SpecificationDefinitionResponse>>> list(
            @RequestParam(required = false) Long categoryId
    ) {

        List<SpecificationDefinitionResponse> responses =
                specificationDefinitionService
                        .listForCategory(categoryId)
                        .stream()
                        .map(SpecificationDefinitionResponse::from)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy danh sách thông số kỹ thuật thành công!",
                        responses
                )
        );
    }
}