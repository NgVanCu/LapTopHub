package com.laptophub.product.dto.response;


import com.laptophub.product.entity.ProductSpecValue;
import com.laptophub.product.entity.SpecificationDefinition;

public record ProductSpecValueResponse(
        Long specificationDefinitionId,
        String code,
        String label,
        String unit,
        String groupLabel,
        String value) {

    public static ProductSpecValueResponse from(
            ProductSpecValue specValue,
            SpecificationDefinition definition) {

        return new ProductSpecValueResponse(
                definition.getId(),
                definition.getCode(),
                definition.getLabel(),
                definition.getUnit(),
                definition.getGroupLabel(),
                specValue.getValue());
    }
}
