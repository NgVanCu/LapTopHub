package com.laptophub.product.service.impl;

import com.laptophub.product.entity.SpecificationDefinition;
import com.laptophub.product.repository.SpecificationDefinitionRepository;
import com.laptophub.product.service.SpecificationDefinitionService;
import com.laptophub.shared.exception.AppException;
import com.laptophub.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SpecificationDefinitionServiceImpl
        implements SpecificationDefinitionService {

    private final SpecificationDefinitionRepository
            specificationDefinitionRepository;

    public SpecificationDefinitionServiceImpl(
            SpecificationDefinitionRepository
                    specificationDefinitionRepository
    ) {
        this.specificationDefinitionRepository =
                specificationDefinitionRepository;
    }

    @Override
    public List<SpecificationDefinition> listAll() {
        return specificationDefinitionRepository
                .findAllByOrderByDisplayOrderAsc();
    }

    @Override
    public List<SpecificationDefinition> listForCategory(
            Long categoryId
    ) {
        if (categoryId == null) {
            return listAll();
        }

        return specificationDefinitionRepository
                .findApplicableToCategory(categoryId);
    }

    @Override
    public SpecificationDefinition getByIdOrThrow(
            Long id
    ) {
        return specificationDefinitionRepository
                .findById(id)
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.RESOURCE_NOT_FOUND
                        )
                );
    }

    @Override
    public Map<Long, SpecificationDefinition> findByIds(
            List<Long> ids
    ) {

        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }

        return specificationDefinitionRepository
                .findAllById(ids)
                .stream()
                .collect(Collectors.toMap(
                        SpecificationDefinition::getId,
                        specificationDefinition ->
                                specificationDefinition
                ));
    }
}