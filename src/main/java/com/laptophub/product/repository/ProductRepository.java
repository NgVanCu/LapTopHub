package com.laptophub.product.repository;

import com.laptophub.product.dto.projection.ProductSearchProjection;
import com.laptophub.product.entity.Product;
import com.laptophub.product.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    boolean existsBySlug(String slug);

    @Query("""
            select p
            from Product p
            where (:categoryId is null or p.categoryId = :categoryId)
              and (:brandId is null or p.brandId = :brandId)
              and (:status is null or p.status = :status)
              and (:keyword is null
                   or lower(p.name) like concat('%', lower(:keyword), '%'))
            """)
    Page<Product> searchAdmin(
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("status") ProductStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /*
     * Public detail.
     *
     * Product chỉ được public nếu:
     * - Product ACTIVE
     * - Category ACTIVE
     * - Brand ACTIVE
     */
    @Query("""
            select p
            from Product p
            where p.slug = :slug
              and p.status = 'ACTIVE'
              and exists (
                  select 1
                  from Category c
                  where c.id = p.categoryId
                    and c.status = 'ACTIVE'
              )
              and exists (
                  select 1
                  from Brand b
                  where b.id = p.brandId
                    and b.status = 'ACTIVE'
              )
            """)
    Optional<Product> findPublicBySlug(
            @Param("slug") String slug
    );

    // value muc đích trang hiện tại có những product nào
    // countquery tổng cộng có bao nhieu product phù hợp với filter
    @Query(
            value = """
                    select
                        p.id as id,
                        p.name as name,
                        p.slug as slug,
                        p.categoryId as categoryId,
                        p.brandId as brandId,
                        p.createdAt as createdAt,
                        min(v.price) as priceFrom,
                        max(v.price) as priceTo
                    from Product p
                    join ProductVariant v
                        on v.productId = p.id
                       and v.status = 'ACTIVE'

                    where p.status = 'ACTIVE'

                      and exists (
                          select 1
                          from Category c
                          where c.id = p.categoryId
                            and c.status = 'ACTIVE'
                      )

                      and exists (
                          select 1
                          from Brand b
                          where b.id = p.brandId
                            and b.status = 'ACTIVE'
                      )

                      and (:categoryId is null
                           or p.categoryId = :categoryId)

                      and (:brandId is null
                           or p.brandId = :brandId)

                      and (:keyword is null
                           or lower(p.name)
                              like concat('%', lower(:keyword), '%'))

                      and (
                          (:minPrice is null and :maxPrice is null)

                          or exists (
                              select 1
                              from ProductVariant vf
                              where vf.productId = p.id
                                and vf.status = 'ACTIVE'
                                and (:minPrice is null
                                     or vf.price >= :minPrice)
                                and (:maxPrice is null
                                     or vf.price <= :maxPrice)
                          )
                      )

                    group by
                        p.id,
                        p.name,
                        p.slug,
                        p.categoryId,
                        p.brandId,
                        p.createdAt
                    """,

            countQuery = """
                    select count(p.id)
                    from Product p

                    where p.status = 'ACTIVE'

                      and exists (
                          select 1
                          from Category c
                          where c.id = p.categoryId
                            and c.status = 'ACTIVE'
                      )

                      and exists (
                          select 1
                          from Brand b
                          where b.id = p.brandId
                            and b.status = 'ACTIVE'
                      )

                      and (:categoryId is null
                           or p.categoryId = :categoryId)

                      and (:brandId is null
                           or p.brandId = :brandId)

                      and (:keyword is null
                           or lower(p.name)
                              like concat('%', lower(:keyword), '%'))

                      and (
                          (:minPrice is null and :maxPrice is null)

                          or exists (
                              select 1
                              from ProductVariant vf
                              where vf.productId = p.id
                                and vf.status = 'ACTIVE'
                                and (:minPrice is null
                                     or vf.price >= :minPrice)
                                and (:maxPrice is null
                                     or vf.price <= :maxPrice)
                          )
                      )
                    """
    )
    Page<ProductSearchProjection> searchPublic(
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("keyword") String keyword,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );
}