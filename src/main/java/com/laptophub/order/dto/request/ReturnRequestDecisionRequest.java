package com.laptophub.order.dto.request;

import jakarta.validation.constraints.Size;

// Dùng cho reject (note giải thích lý do từ chối, tùy chọn). approve không
// cần body.
public record ReturnRequestDecisionRequest(

        @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
        String note) {
}

