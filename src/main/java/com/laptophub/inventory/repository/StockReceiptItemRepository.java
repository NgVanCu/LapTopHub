package com.laptophub.inventory.repository;

import com.laptophub.inventory.entity.StockReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface StockReceiptItemRepository extends JpaRepository<StockReceiptItem, Long> {

    List<StockReceiptItem> findByStockReceiptId(Long stockReceiptId);

    // Dùng bởi StockReceiptService.replaceItems (full-replace: xoá hết rồi tạo
    // lại). PHẢI khai @Query tường minh (không dùng derived "deleteByX" suông)
    // — derived delete nạp entity vào action queue rồi remove() qua Hibernate,
    // và Hibernate flush INSERT trước DELETE theo thứ tự mặc định của action
    // queue, nên nếu dòng mới tái dùng đúng 1 productVariantId đã có, INSERT
    // chạy trước sẽ đụng unique constraint (stock_receipt_id,
    // product_variant_id) của dòng CŨ chưa kịp xoá. @Query + @Modifying biến
    // đây thành 1 câu lệnh bulk DELETE thực thi ngay (executeUpdate), tách
    // hẳn khỏi action queue; flushAutomatically đảm bảo mọi thay đổi đang chờ
    // được đẩy xuống DB trước khi DELETE chạy.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from StockReceiptItem s where s.stockReceiptId = :stockReceiptId")
    void deleteByStockReceiptId(@Param("stockReceiptId") Long stockReceiptId);
}
