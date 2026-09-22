package com.laptophub.product.dto.response;

import com.laptophub.product.entity.SpecificationDefinition;

public record SpecificationDefinitionResponse(
        Long id,
        String code,
        String label,
        String unit,
        String groupLabel,
        int displayOrder) {

    public static SpecificationDefinitionResponse from(
            SpecificationDefinition specification) {

        return new SpecificationDefinitionResponse(
                specification.getId(),
                specification.getCode(),
                specification.getLabel(),
                specification.getUnit(),
                specification.getGroupLabel(),
                specification.getDisplayOrder());
    }
}