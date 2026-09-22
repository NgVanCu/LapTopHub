package com.laptophub.product.repository;

import com.laptophub.product.entity.ProductSpecValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSpecValueRepository extends JpaRepository<ProductSpecValue, Long> {
    List<ProductSpecValue> findByProductId(Long productId);

    // Batch cho nhiều sản phẩm cùng lúc (VD: so sánh sản phẩm) — tránh N+1.
    List<ProductSpecValue> findByProductIdIn(List<Long> productIds);

    // Phục vụ upsert kiểu "thay toàn bộ": xóa các giá trị cũ không còn nằm
    // trong danh sách specificationDefinitionId mới trước khi lưu/ghi đè
    // phần còn lại — clearAutomatically để tránh persistence context giữ
    // tham chiếu tới bản ghi đã xóa (cùng lý do RefreshTokenRepository).
    @Modifying(clearAutomatically = true)
    @Query("delete from ProductSpecValue v where v.productId = :productId and v.specificationDefinitionId not in :specificationDefinitionIds")
    void deleteByProductIdAndSpecificationDefinitionIdNotIn(Long productId, List<Long> specificationDefinitionIds);
}
