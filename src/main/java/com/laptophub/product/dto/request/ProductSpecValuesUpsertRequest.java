package com.laptophub.product.dto.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProductSpecValuesUpsertRequest(

        @NotNull
        List<@Valid SpecValueItem> values) {

    public record SpecValueItem(

            @NotNull(message = "specificationDefinitionId không được để trống")
            Long specificationDefinitionId,

            @NotBlank(message = "Giá trị không được để trống")
            String value) {
    }
}
