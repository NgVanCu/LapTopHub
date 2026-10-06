package com.laptophub.order.service.impl;

import com.laptophub.cart.entity.CartItem;
import com.laptophub.cart.service.CartService;
import com.laptophub.inventory.service.InventoryService;
import com.laptophub.order.dto.projection.BestSellingProjection;
import com.laptophub.order.dto.projection.OrderStatusCountProjection;
import com.laptophub.order.dto.projection.RevenueByDayProjection;
import com.laptophub.order.dto.request.CheckoutRequest;
import com.laptophub.order.dto.response.CheckoutResult;
import com.laptophub.order.dto.response.OrderItemResponse;
import com.laptophub.order.dto.response.OrderSummaryResponse;
import com.laptophub.order.entity.Order;
import com.laptophub.order.entity.OrderItem;
import com.laptophub.order.entity.OrderStatusHistory;
import com.laptophub.order.enums.OrderStatus;
import com.laptophub.order.enums.PaymentMethod;
import com.laptophub.order.repository.OrderItemRepository;
import com.laptophub.order.repository.OrderRepository;
import com.laptophub.order.repository.OrderStatusHistoryRepository;
import com.laptophub.order.service.OrderService;
import com.laptophub.payment.entity.Payment;
import com.laptophub.payment.enums.PaymentStatus;
import com.laptophub.payment.service.PaymentService;
import com.laptophub.product.entity.Product;
import com.laptophub.product.entity.ProductVariant;
import com.laptophub.product.enums.ProductStatus;
import com.laptophub.product.enums.ProductVariantStatus;
import com.laptophub.product.service.ProductService;
import com.laptophub.product.service.ProductVariantService;
import com.laptophub.shared.exception.AppException;
import com.laptophub.shared.exception.ErrorCode;
import com.laptophub.user.entity.Address;
import com.laptophub.user.service.AddressService;
import com.laptophub.voucher.entity.Voucher;
import com.laptophub.voucher.service.VoucherService;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final CartService cartService;
    private final AddressService addressService;
    private final ProductVariantService productVariantService;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final VoucherService voucherService;
    private final PaymentService paymentService;

    public OrderServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                        OrderStatusHistoryRepository orderStatusHistoryRepository, CartService cartService,
                        AddressService addressService, ProductVariantService productVariantService,
                        ProductService productService, InventoryService inventoryService, VoucherService voucherService,
                        PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.cartService = cartService;
        this.addressService = addressService;
        this.productVariantService = productVariantService;
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.voucherService = voucherService;
        this.paymentService = paymentService;
    }

    // Tạo đơn + reserve tồn trong cùng 1 transaction (DATABASE_DESIGN.md §4).
    // lockItemsForCheckout khoá pessimistic trên Cart ngay từ đầu, serialize
    // các request checkout đồng thời của cùng user — tránh double-order khi
    // double-click/network retry (API_CONVENTION.md §8).
    @Transactional
    public CheckoutResult checkout(Long userId, CheckoutRequest request) {
        List<CartItem> cartItems = cartService.lockItemsForCheckout(userId);
        if (cartItems.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Giỏ hàng trống");
        }

        Address address = addressService.getOwned(request.addressId());

        List<CheckoutLine> lines = cartItems.stream().map(this::resolveLine).toList();

        BigDecimal rawTotal = lines.stream()
                .map(line -> line.variant().getPrice().multiply(BigDecimal.valueOf(line.cartItem().getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Voucher được validate (không redeem) TRƯỚC khi tạo Order/OrderItem —
        // chỉ cần biết discountAmount để tính totalAmount cuối cùng. redeem()
        // (tăng used_count + ghi VoucherUsage) xảy ra SAU khi Order đã có id
        // (bước bên dưới), vì VoucherUsage cần orderId thật.
        Voucher voucher = null;
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (request.voucherCode() != null) {
            var validation = voucherService.validate(request.voucherCode(), userId, rawTotal);
            voucher = validation.voucher();
            discountAmount = validation.discountAmount();
        }
        BigDecimal totalAmount = rawTotal.subtract(discountAmount);
        PaymentMethod paymentMethod = request.paymentMethod() != null ? request.paymentMethod() : PaymentMethod.COD;

        Order order = orderRepository.saveAndFlush(Order.create(userId, paymentMethod, totalAmount, discountAmount,
                voucher != null ? voucher.getId() : null, voucher != null ? voucher.getCode() : null, request.note(),
                address.getRecipientName(), address.getPhone(), address.getProvince(),
                address.getWard(), address.getStreetAddress()));

        List<OrderItem> items = orderItemRepository.saveAllAndFlush(lines.stream()
                .map(line -> OrderItem.create(order.getId(), line.variant().getId(), line.product().getName(),
                        line.variant().getVariantName(), line.variant().getSku(), line.variant().getPrice(),
                        line.cartItem().getQuantity()))
                .toList());

        // redeem() gọi incrementUsedCount (@Modifying(clearAutomatically = true))
        // — xoá persistence context, nhưng Order/OrderItem phía trên không cần
        // đọc lại (chỉ trả về object Java hiện có ở CheckoutResult), nên an toàn
        // để làm bước này TRƯỚC vòng lặp reserve tồn kho bên dưới.
        if (voucher != null) {
            voucherService.redeem(voucher.getId(), order.getId(), userId, discountAmount);
        }

        // InventoryBalanceRepository dùng @Modifying(clearAutomatically = true) —
        // mỗi lần reserve() xoá persistence context, Order/OrderItem phía trên bị
        // detach. Không sao vì không cần mutate lại chúng sau bước này.
        for (OrderItem item : items) {
            inventoryService.reserve(item.getProductVariantId(), item.getQuantity(), "ORDER_ITEM", item.getId());
        }

        // Chỉ tạo dòng payments cho ONLINE — COD không cần theo dõi trạng thái
        // thanh toán qua cổng nào cả (tiền thu khi giao hàng).
        if (paymentMethod == PaymentMethod.ONLINE) {
            paymentService.createForOrder(order.getId(), order.getTotalAmount());
        }

        cartService.clear(userId);

        return new CheckoutResult(order, items);
    }

    public Page<Order> listByUser(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable);
    }

    // Không phân biệt "không tồn tại" và "không phải của user này" — cả 2
    // đều RESOURCE_NOT_FOUND, giống AddressService.getOwned.
    public Order getOwnedOrThrow(Long userId, Long orderId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    public List<OrderItem> getItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    // Dùng bởi Customer/AdminOrderController để build OrderResponse.paymentStatus
    // — null cho đơn COD (không có Payment). Trạng thái Payment thật sự chỉ đổi
    // qua /ipn (PublicVnPayController), đây chỉ đọc lại để Frontend biết đơn
    // ONLINE đã thanh toán xong hay chưa mà không cần đợi Order.status đổi
    // (Order chỉ chuyển PENDING -> CONFIRMED khi Admin xác nhận thủ công, tách
    // biệt với việc Payment đã PAID hay chưa).
    public PaymentStatus getPaymentStatus(Long orderId) {
        return paymentService.findByOrderId(orderId).map(Payment::getStatus).orElse(null);
    }

    // Dùng bởi Customer/AdminOrderController cho GET danh sách đơn — batch 1
    // lần cho cả trang thay vì gọi getPaymentStatus từng dòng, tránh N+1.
    public Page<OrderSummaryResponse> toSummaryResponses(Page<Order> orders) {
        List<Long> orderIds = orders.getContent().stream().map(Order::getId).toList();
        Map<Long, PaymentStatus> statusByOrderId = paymentService.findStatusesByOrderIds(orderIds);
        return orders.map(order -> OrderSummaryResponse.from(order, statusByOrderId.get(order.getId())));
    }

    // Dùng bởi Customer/AdminOrderController để build OrderResponse.items —
    // productId/productSlug không snapshot trên OrderItem (chỉ productVariantId)
    // nên resolve sống ở đây, batch 1 lần cho cả đơn để tránh N+1 (đúng pattern
    // ProductComparisonService/CustomerCartController.enrich).
    public List<OrderItemResponse> toItemResponses(List<OrderItem> items) {
        List<Long> variantIds = items.stream().map(OrderItem::getProductVariantId).distinct().toList();
        Map<Long, Long> productIdByVariantId = productVariantService.findProductIdsByVariantIds(variantIds);
        List<Long> productIds = productIdByVariantId.values().stream().distinct().toList();
        Map<Long, Product> productsById = productService.findByIds(productIds);
        return items.stream()
                .map(item -> {
                    Long productId = productIdByVariantId.get(item.getProductVariantId());
                    Product product = productId != null ? productsById.get(productId) : null;
                    return OrderItemResponse.from(item, productId, product != null ? product.getSlug() : null);
                })
                .toList();
    }

    // Dùng bởi dashboard module (Giai đoạn 9) — trả dữ liệu thô theo ngày,
    // không biết gì về DTO của dashboard (giữ đúng ranh giới module qua
    // service). from/to null nghĩa là không giới hạn phía đó.
    public List<RevenueByDayProjection> getRevenueByDay(Instant from, Instant to) {
        return orderRepository.findRevenueByDay(from, to);
    }

    // Dùng bởi dashboard module — không lọc status (khác getRevenueByDay),
    // muốn thấy toàn bộ vòng đời đơn hàng kể cả PENDING/CANCELLED.
    public List<OrderStatusCountProjection> countOrdersByStatus(Instant from, Instant to) {
        return orderRepository.countByStatusGrouped(from, to);
    }

    // Dùng bởi dashboard module — top N ProductVariant bán chạy nhất (theo
    // số lượng), chỉ tính đơn DELIVERED giống getRevenueByDay. limit dùng
    // PageRequest.of(0, limit) chỉ để giới hạn số dòng, không phải phân
    // trang thật (xem OrderItemRepository.findBestSelling).
    public List<BestSellingProjection> findBestSellingVariants(Instant from, Instant to, int limit) {
        return orderItemRepository.findBestSelling(from, to, PageRequest.of(0, limit));
    }

    // Dùng bởi review module (qua service, không đọc thẳng repository của
    // order) để kiểm tra quyền đánh giá: user đã có đơn DELIVERED chứa sản
    // phẩm này chưa. Trả về orderId của lần mua DELIVERED gần nhất (dùng làm
    // snapshot "verified purchase" khi tạo Review) — Optional.empty() nếu
    // chưa từng mua sản phẩm này ở đơn đã giao. Map variantId -> productId
    // được resolve qua ProductVariantService (catalog), không tự query
    // repository của catalog — giữ đúng nguyên tắc "giao tiếp qua service".
    public Optional<Long> findDeliveredOrderIdForProduct(Long userId, Long productId) {
        List<OrderItem> deliveredItems = orderItemRepository.findByOrderUserIdAndOrderStatus(userId,
                OrderStatus.DELIVERED);
        if (deliveredItems.isEmpty()) {
            return Optional.empty();
        }
        Map<Long, Long> productIdByVariantId = productVariantService.findProductIdsByVariantIds(
                deliveredItems.stream().map(OrderItem::getProductVariantId).distinct().toList());
        return deliveredItems.stream()
                .filter(item -> productId.equals(productIdByVariantId.get(item.getProductVariantId())))
                .max(Comparator.comparing(OrderItem::getCreatedAt))
                .map(OrderItem::getOrderId);
    }

    // Dùng cho Admin xem/thao tác bất kỳ đơn nào, không giới hạn theo chủ đơn
    // (khác getOwnedOrThrow).
    public Order getByIdOrThrow(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    public Page<Order> listAdmin(OrderStatus status, Pageable pageable) {
        return orderRepository.search(status, pageable);
    }

    // confirm/prepare/deliver không đụng inventory nên không có rủi ro
    // clearAutomatically — nhưng vẫn phải saveAndFlush tường minh sau khi
    // mutate: dựa vào auto-flush ngầm của Hibernate (chỉ chắc chắn xảy ra khi
    // transaction thật sự commit) không đáng tin cậy khi nhiều lệnh gọi
    // @Transactional nối tiếp cùng tham gia 1 transaction (vd nhiều request
    // liên tiếp trong 1 test @Transactional) — thay đổi có thể chỉ nằm trên
    // object Java và bị mất khi request kế tiếp đọc lại. saveAndFlush ép ghi
    // UPDATE ngay lập tức, không phụ thuộc thời điểm auto-flush.
    // Gate thanh toán: đơn ONLINE chưa PAID không được Admin confirm — tránh
    // xác nhận/chuẩn bị/xuất hàng cho đơn chưa hề được thanh toán. Chỉ chặn ở
    // đây (bước đầu state machine); prepare/ship/deliver không cần kiểm tra
    // lại vì đơn đã qua được confirm. Không đồng bộ Payment->Order lúc IPN
    // (tránh phụ thuộc vòng Payment->Order) — thay vào đó kiểm tra "lười"
    // ngay tại thời điểm Admin hành động.
    @Transactional
    public Order confirm(Long orderId, Long actingUserId) {
        Order order = getByIdOrThrow(orderId);
        if (order.getPaymentMethod() == PaymentMethod.ONLINE) {
            Payment payment = paymentService.findByOrderId(orderId).orElse(null);
            if (payment == null || payment.getStatus() != PaymentStatus.PAID) {
                throw new AppException(ErrorCode.INVALID_PAYMENT_STATUS,
                        "Đơn thanh toán online chưa hoàn tất, không thể xác nhận");
            }
        }
        OrderStatus previous = order.getStatus();
        order.confirm();
        orderRepository.saveAndFlush(order);
        recordHistory(orderId, previous, order.getStatus(), actingUserId, null);
        return order;
    }

    @Transactional
    public Order prepare(Long orderId, Long actingUserId) {
        Order order = getByIdOrThrow(orderId);
        OrderStatus previous = order.getStatus();
        order.prepare();
        orderRepository.saveAndFlush(order);
        recordHistory(orderId, previous, order.getStatus(), actingUserId, null);
        return order;
    }

    @Transactional
    public Order deliver(Long orderId, Long actingUserId) {
        Order order = getByIdOrThrow(orderId);
        OrderStatus previous = order.getStatus();
        order.deliver();
        orderRepository.saveAndFlush(order);
        recordHistory(orderId, previous, order.getStatus(), actingUserId, null);
        return order;
    }

    // Gate atomic (OrderRepository.shipIfPreparing) chạy TRƯỚC vòng lặp gọi
    // InventoryService.fulfill — chỉ transaction thắng cuộc đua giành được
    // quyền chuyển PREPARING->SHIPPING mới được trừ tồn. Chống race 2 request
    // ship() đồng thời cùng 1 đơn (double-fulfill nếu tồn kho variant đủ
    // lớn). getByIdOrThrow đầu chỉ để trả đúng 404 khi id không tồn tại.
    @Transactional
    public Order ship(Long orderId, Long actingUserId) {
        getByIdOrThrow(orderId);

        int rows = orderRepository.shipIfPreparing(orderId);
        if (rows == 0) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
        }

        List<OrderItem> items = getItems(orderId);
        for (OrderItem item : items) {
            inventoryService.fulfill(item.getProductVariantId(), item.getQuantity(), "ORDER_ITEM", item.getId());
        }

        Order managedOrder = getByIdOrThrow(orderId);
        recordHistory(orderId, OrderStatus.PREPARING, managedOrder.getStatus(), actingUserId, null);
        return managedOrder;
    }

    // Admin hủy được tới cả PREPARING (vd phát hiện hết hàng lúc soạn) — dùng
    // đúng tập cho phép rộng nhất của Order.isCancellable().
    @Transactional
    public Order cancelByAdmin(Long orderId, Long actingUserId) {
        Order order = getByIdOrThrow(orderId);
        if (!order.isCancellable()) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
        }
        return cancelInternal(orderId, order.getStatus(), actingUserId, "Admin hủy đơn");
    }

    // Customer chỉ tự hủy được khi đơn chưa bắt đầu chuẩn bị (PENDING/
    // CONFIRMED) — phạm vi hẹp hơn năng lực Admin, kiểm tra ở đây TRƯỚC khi
    // đụng inventory, không dựa vào Order.isCancellable() (rộng hơn).
    @Transactional
    public Order cancelByCustomer(Long userId, Long orderId) {
        Order order = getOwnedOrThrow(userId, orderId);
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS,
                    "Đơn đã bắt đầu chuẩn bị, vui lòng liên hệ để được hỗ trợ hủy đơn");
        }
        return cancelInternal(orderId, order.getStatus(), userId, "Customer hủy đơn");
    }

    // Gate atomic (OrderRepository.cancelIfCancellable) chạy TRƯỚC vòng lặp
    // gọi InventoryService.release — chỉ transaction thắng cuộc đua giành
    // được quyền chuyển sang CANCELLED mới được release tồn. KHÔNG dựa vào
    // điều kiện reservedQuantity >= quantity của InventoryBalanceRepository
    // để chống double-release: reservedQuantity là tổng hợp của MỌI đơn đang
    // giữ hàng cho variant đó, nên nếu có đơn khác cũng đang giữ hàng,
    // reservedQuantity sau lần release thứ nhất vẫn có thể đủ lớn để lần
    // release thứ hai (do double-cancel) tiếp tục "trông như" hợp lệ.
    private Order cancelInternal(Long orderId, OrderStatus previous, Long actingUserId, String note) {
        int rows = orderRepository.cancelIfCancellable(orderId);
        if (rows == 0) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
        }

        for (OrderItem item : getItems(orderId)) {
            inventoryService.release(item.getProductVariantId(), item.getQuantity(), "ORDER_ITEM", item.getId());
        }

        Order managedOrder = getByIdOrThrow(orderId);
        recordHistory(orderId, previous, managedOrder.getStatus(), actingUserId, note);
        // Best-effort dọn payment đang treo (đơn ONLINE chưa thanh toán bị hủy) —
        // no-op nếu đơn COD (không có payment) hoặc payment đã ở trạng thái cuối
        // (bao gồm cả trường hợp đã bị cancelExpiredOnlineOrder hủy từ trước, xem
        // method đó — cuộc gọi này khi ấy chỉ no-op, tự nhiên idempotent).
        paymentService.cancelIfPending(orderId);
        return managedOrder;
    }

    // Gọi bởi PaymentExpirySweepScheduler cho từng payment quá hạn ứng viên.
    // Payment-gate (paymentService.cancelIfExpired) PHẢI chạy và thắng TRƯỚC
    // KHI đụng Order/inventory — atomically xác nhận LẠI ngay tại thời điểm
    // xử lý rằng payment vẫn chưa PAID và vẫn thực sự hết hạn (không dựa vào
    // kết quả liệt kê candidate đã cũ). Nếu không "chiếm quyền" được (đã PAID
    // bởi IPN, đã CANCELLED, hoặc vừa được customer gia hạn qua retry) thì
    // return ngay, không có side effect nào — đảm bảo không bao giờ xảy ra
    // Payment=PAID nhưng Order=CANCELLED+tồn đã release. actingUserId=null
    // nghĩa là hệ thống tự động (xem migration V28 + OrderStatusHistory).
    @Transactional
    public void cancelExpiredOnlineOrder(Long orderId, Instant now) {
        Payment payment = paymentService.findByOrderId(orderId).orElse(null);
        if (payment == null) {
            return;
        }
        boolean claimed = paymentService.cancelIfExpired(payment.getId(), now);
        if (!claimed) {
            return;
        }
        OrderStatus previous = getByIdOrThrow(orderId).getStatus();
        cancelInternal(orderId, previous, null, "Hệ thống tự hủy: quá hạn thanh toán");
    }

    // Không đụng inventory nên không cần load lại — nhưng vẫn saveAndFlush
    // tường minh (xem giải thích ở confirm/prepare/deliver).
    @Transactional
    public Order markReturnRequested(Long orderId, Long actingUserId) {
        Order order = getByIdOrThrow(orderId);
        OrderStatus previous = order.getStatus();
        order.requestReturn();
        orderRepository.saveAndFlush(order);
        recordHistory(orderId, previous, order.getStatus(), actingUserId, null);
        return order;
    }

    // Gate atomic (OrderRepository.approveReturnIfRequested) chạy TRƯỚC vòng
    // lặp gọi InventoryService.receiveReturn — chỉ transaction thắng cuộc đua
    // giành được quyền chuyển RETURN_REQUESTED->RETURNED mới được hoàn tồn.
    // Chống race 2 request approve đồng thời cùng 1 return request (double
    // receiveReturn). Không cần getByIdOrThrow đầu: orderId luôn tồn tại vì
    // ReturnRequest.orderId có FK tới orders.id — không thể có return request
    // mồ côi. ReturnRequestService.approve() là caller DUY NHẤT của method
    // này (đã xác nhận) nên gate ở đây là đủ, không cần thêm gate ở
    // ReturnRequestRepository.
    @Transactional
    public Order approveReturn(Long orderId, Long actingUserId) {
        int rows = orderRepository.approveReturnIfRequested(orderId);
        if (rows == 0) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
        }

        for (OrderItem item : getItems(orderId)) {
            inventoryService.receiveReturn(item.getProductVariantId(), item.getQuantity(), "ORDER_ITEM",
                    item.getId());
        }

        Order managedOrder = getByIdOrThrow(orderId);
        recordHistory(orderId, OrderStatus.RETURN_REQUESTED, managedOrder.getStatus(), actingUserId,
                "Chấp nhận yêu cầu trả hàng");
        return managedOrder;
    }

    // Không đụng inventory (hàng chưa từng được nhận lại) nên không cần load
    // lại.
    @Transactional
    public Order rejectReturn(Long orderId, Long actingUserId) {
        Order order = getByIdOrThrow(orderId);
        OrderStatus previous = order.getStatus();
        order.rejectReturn();
        orderRepository.saveAndFlush(order);
        recordHistory(orderId, previous, order.getStatus(), actingUserId, "Từ chối yêu cầu trả hàng");
        return order;
    }

    private void recordHistory(Long orderId, OrderStatus from, OrderStatus to, Long changedByUserId, String note) {
        orderStatusHistoryRepository.save(OrderStatusHistory.create(orderId, from, to, changedByUserId, note));
    }

    private CheckoutLine resolveLine(CartItem cartItem) {
        ProductVariant variant = productVariantService.getByIdOrThrow(cartItem.getProductVariantId());
        Product product = productService.getByIdOrThrow(variant.getProductId());
        if (variant.getStatus() != ProductVariantStatus.ACTIVE || product.getStatus() != ProductStatus.ACTIVE) {
            throw new AppException(ErrorCode.PRODUCT_VARIANT_UNAVAILABLE,
                    "Sản phẩm \"" + product.getName() + "\" hiện không khả dụng để đặt hàng");
        }
        return new CheckoutLine(cartItem, variant, product);
    }

    private record CheckoutLine(CartItem cartItem, ProductVariant variant, Product product) {
    }
}
