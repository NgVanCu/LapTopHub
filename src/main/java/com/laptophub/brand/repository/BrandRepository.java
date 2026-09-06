package com.laptophub.brand.repository;

import com.laptophub.brand.entity.Brand;
import com.laptophub.brand.enums.BrandStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    Optional<Brand> findBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    boolean existsBySlug(String slug);

    List<Brand> findByStatusOrderByNameAsc(BrandStatus status);
}
