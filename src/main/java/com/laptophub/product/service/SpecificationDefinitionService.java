package com.laptophub.product.service;

import com.laptophub.product.entity.SpecificationDefinition;

import java.util.List;
import java.util.Map;

public interface SpecificationDefinitionService {

    List<SpecificationDefinition> listAll();

    List<SpecificationDefinition> listForCategory(Long categoryId);

    SpecificationDefinition getByIdOrThrow(Long id);

    Map<Long, SpecificationDefinition> findByIds(List<Long> ids);
}
