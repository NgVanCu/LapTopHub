package com.laptophub.order.service;

import com.laptophub.order.dto.projection.BestSellingProjection;
import com.laptophub.order.dto.projection.OrderStatusCountProjection;
import com.laptophub.order.dto.projection.RevenueByDayProjection;
import com.laptophub.order.dto.request.CheckoutRequest;
import com.laptophub.order.dto.response.CheckoutResult;
import com.laptophub.order.dto.response.OrderItemResponse;
import com.laptophub.order.dto.response.OrderSummaryResponse;
import com.laptophub.order.entity.Order;
import com.laptophub.order.entity.OrderItem;
import com.laptophub.order.enums.OrderStatus;
import com.laptophub.payment.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderService {

    CheckoutResult checkout(Long userId, CheckoutRequest request);

    Page<Order> listByUser(Long userId,
                           Pageable pageable);

    Order getOwnedOrThrow(Long userId, Long orderId);

    List<OrderItem> getItems(Long orderId);

    PaymentStatus getPaymentStatus(Long orderId);

    Page<OrderSummaryResponse> toSummaryResponses(Page<Order> orders);

    List<OrderItemResponse> toItemResponses(List<OrderItem> items);

    List<RevenueByDayProjection> getRevenueByDay(Instant from, Instant to);

    List<OrderStatusCountProjection> countOrdersByStatus(Instant from, Instant to);

    List<BestSellingProjection> findBestSellingVariants(Instant from, Instant to, int limit);

    Optional<Long> findDeliveredOrderIdForProduct(Long userId, Long productId);

    Order getByIdOrThrow(Long orderId);

    Page<Order> listAdmin(OrderStatus status, Pageable pageable);

    Order confirm(Long orderId, Long actingUserId);

    Order prepare(Long orderId, Long actingUserId);

    Order deliver(Long orderId, Long actingUserId);

    Order ship(Long orderId, Long actingUserId);

    Order cancelByAdmin(Long orderId, Long actingUserId);

    Order cancelByCustomer(Long userId, Long orderId);

    void cancelExpiredOnlineOrder(Long orderId, Instant now);

    Order markReturnRequested(Long orderId, Long actingUserId);

    Order approveReturn(Long orderId, Long actingUserId);

    Order rejectReturn(Long orderId, Long actingUserId);
}
