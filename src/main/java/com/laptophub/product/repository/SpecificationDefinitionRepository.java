package com.laptophub.product.repository;

import com.laptophub.product.entity.SpecificationDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecificationDefinitionRepository extends JpaRepository<SpecificationDefinition, Long> {

    List<SpecificationDefinition> findAllByOrderByDisplayOrderAsc();

    // categoryId = null -> chỉ spec toàn cục; có giá trị -> spec toàn cục +
    // spec riêng của danh mục đó.
    @Query("""
            select s from SpecificationDefinition s
            where s.categoryId is null or s.categoryId = :categoryId
            order by s.displayOrder asc
            """)
    List<SpecificationDefinition> findApplicableToCategory(Long categoryId);
}
