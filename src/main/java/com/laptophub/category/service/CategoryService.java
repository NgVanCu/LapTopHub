package com.laptophub.category.service;

import com.laptophub.category.dto.request.CategoryCreateRequest;
import com.laptophub.category.dto.request.CategoryUpdateRequest;
import com.laptophub.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    Category create(CategoryCreateRequest request);

    Category update(Long id, CategoryUpdateRequest request);

    Category getByIdOrThrow(Long id);

    Page<Category> list(Pageable pageable);

    List<Category> listActive();

    Category activate(Long id);

    Category deactivate(Long id);
}
