package com.laptophub.storage.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BatchPresignRequest(
        @NotEmpty(message = "Danh sách ảnh không được để trống")
        @Size(max = 10, message = "Tối đa 10 ảnh mỗi lần")
        List<@Valid PresignRequest> files
) {}
