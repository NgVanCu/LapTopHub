package com.laptophub.order.service.impl;

import com.laptophub.order.entity.Order;
import com.laptophub.order.entity.ReturnRequest;
import com.laptophub.order.enums.OrderStatus;
import com.laptophub.order.enums.ReturnRequestStatus;
import com.laptophub.order.repository.ReturnRequestRepository;
import com.laptophub.order.service.OrderService;
import com.laptophub.order.service.ReturnRequestService;
import com.laptophub.shared.exception.AppException;
import com.laptophub.shared.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ReturnRequestServiceImpl implements ReturnRequestService {
    private final ReturnRequestRepository returnRequestRepository;
    private final OrderService orderService;

    public ReturnRequestServiceImpl(ReturnRequestRepository returnRequestRepository, OrderService orderService) {
        this.returnRequestRepository = returnRequestRepository;
        this.orderService = orderService;
    }

    // Customer tạo yêu cầu trả hàng cho đơn của chính mình — chỉ khi đơn đã
    // DELIVERED và chưa có request nào khác đang REQUESTED cho đơn đó.
    @Override
    @Transactional
    public ReturnRequest create(Long userId, Long orderId, String reason) {
        Order order = orderService.getOwnedOrThrow(userId, orderId);
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Chỉ có thể yêu cầu trả hàng khi đơn đã giao");
        }
        if (returnRequestRepository.existsByOrderIdAndStatus(orderId, ReturnRequestStatus.REQUESTED)) {
            throw new AppException(ErrorCode.RESOURCE_CONFLICT, "Đơn đã có yêu cầu trả hàng đang chờ xử lý");
        }

        ReturnRequest request = returnRequestRepository.save(ReturnRequest.create(orderId, userId, reason));
        // markReturnRequested không đụng inventory nên không có rủi ro
        // clearAutomatically với `request` vừa save ở trên.
        orderService.markReturnRequested(orderId, userId);
        return request;
    }

    @Override
    @Transactional
    public ReturnRequest approve(Long requestId, Long actingAdminId) {
        ReturnRequest request = getByIdOrThrow(requestId);
        if (request.getStatus() != ReturnRequestStatus.REQUESTED) {
            throw new AppException(ErrorCode.INVALID_RETURN_REQUEST_STATUS);
        }
        Long orderId = request.getOrderId();

        // approveReturn() nội bộ gọi InventoryService.receiveReturn
        // (@Modifying(clearAutomatically = true)) — xoá persistence context,
        // `request` đã load ở trên bị detach. Phải load lại managed entity
        // mới trước khi mutate, giống OrderService.ship/cancelInternal.
        orderService.approveReturn(orderId, actingAdminId);
        ReturnRequest managedRequest = getByIdOrThrow(requestId);
        managedRequest.approve(actingAdminId, Instant.now());
        return managedRequest;
    }

    @Override
    @Transactional
    public ReturnRequest reject(Long requestId, Long actingAdminId, String decisionNote) {
        ReturnRequest request = getByIdOrThrow(requestId);
        if (request.getStatus() != ReturnRequestStatus.REQUESTED) {
            throw new AppException(ErrorCode.INVALID_RETURN_REQUEST_STATUS);
        }
        Long orderId = request.getOrderId();

        // rejectReturn() không đụng inventory nên không có rủi ro
        // clearAutomatically — `request` vẫn managed, không cần load lại.
        orderService.rejectReturn(orderId, actingAdminId);
        request.reject(actingAdminId, Instant.now(), decisionNote);
        return request;
    }
    @Override
    public ReturnRequest getByIdOrThrow(Long requestId) {
        return returnRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    // Không phân biệt "không tồn tại" và "không phải của user này" — cả 2 đều
    // RESOURCE_NOT_FOUND, giống AddressService.getOwned.
    @Override
    public ReturnRequest getOwnedOrThrow(Long userId, Long requestId) {
        return returnRequestRepository.findByIdAndUserId(requestId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    public Page<ReturnRequest> listAdmin(ReturnRequestStatus status, Pageable pageable) {
        return returnRequestRepository.search(status, pageable);
    }
}
