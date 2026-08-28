package com.laptophub.shared.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Yêu cầu không hợp lệ"),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "Chưa xác thực"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Không có quyền truy cập"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy dữ liệu"),
    RESOURCE_CONFLICT(HttpStatus.CONFLICT, "Dữ liệu đã tồn tại hoặc xung đột"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email đã được đăng ký"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng"),
    ACCOUNT_BLOCKED(HttpStatus.FORBIDDEN, "Tài khoản đã bị khóa"),
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT, "Không đủ tồn kho khả dụng"),
    INVALID_STOCK_RECEIPT_STATUS(HttpStatus.BAD_REQUEST, "Trạng thái phiếu nhập không hợp lệ cho thao tác này"),
    PRODUCT_VARIANT_UNAVAILABLE(HttpStatus.BAD_REQUEST, "Sản phẩm hiện không khả dụng để đặt hàng"),
    INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "Trạng thái đơn hàng không hợp lệ cho thao tác này"),
    INVALID_RETURN_REQUEST_STATUS(HttpStatus.BAD_REQUEST, "Trạng thái yêu cầu trả hàng không hợp lệ cho thao tác này"),
    VOUCHER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy voucher"),
    VOUCHER_NOT_APPLICABLE(HttpStatus.BAD_REQUEST, "Voucher không áp dụng được cho đơn hàng này"),
    INVALID_PAYMENT_STATUS(HttpStatus.BAD_REQUEST, "Trạng thái thanh toán không hợp lệ cho thao tác này"),
    REVIEW_NOT_ELIGIBLE(HttpStatus.FORBIDDEN, "Bạn cần mua và nhận sản phẩm này trước khi đánh giá"),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "Bạn đã đánh giá sản phẩm này rồi"),
    ACCOUNT_NOT_VERIFIED(HttpStatus.FORBIDDEN, "Tài khoản chưa xác thực email"),
    EMAIL_VERIFICATION_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "Token xác thực không hợp lệ"),
    EMAIL_VERIFICATION_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "Token xác thực đã hết hạn"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống"),
    FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE,"File vượt quá kích thước cho phép");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
