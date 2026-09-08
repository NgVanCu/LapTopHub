package com.laptophub.category.dto.response;

import com.laptophub.category.entity.Category;

public record CategoryPublicResponse(Long id, String name, String slug) {

    public static CategoryPublicResponse from(Category category) {
        return new CategoryPublicResponse(category.getId(), category.getName(), category.getSlug());
    }
}
