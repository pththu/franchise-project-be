package franchiseproject.product_service.repository;

import franchiseproject.product_service.entity.Product;
import franchiseproject.product_service.enums.ProductColor;
import franchiseproject.product_service.enums.ProductSize;
import franchiseproject.product_service.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>,
        JpaSpecificationExecutor<Product> {

    Page<Product> findAll(Pageable pageable);

    @Query("""
        SELECT DISTINCT p
        FROM Product p
        LEFT JOIN FETCH p.variants v
        LEFT JOIN FETCH p.category c
        WHERE (
                :keyword IS NULL OR
                LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) OR
                LOWER(p.brand) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) OR
                LOWER(c.name) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))
              )
          AND (:categoryName IS NULL OR c.name = CAST(:categoryName AS string))
          AND (:status IS NULL OR p.status = :status)
          AND (:size IS NULL OR v.size = :size)
          AND (:color IS NULL OR v.color = :color)
          AND (
              (:fromPrice IS NOT NULL AND :toPrice IS NOT NULL AND (v.salePrice BETWEEN :fromPrice AND :toPrice))
              OR (:fromPrice IS NOT NULL AND :toPrice IS NULL AND v.salePrice >= :fromPrice)
              OR (:fromPrice IS NULL AND :toPrice IS NOT NULL AND v.salePrice <= :toPrice)
              OR (:fromPrice IS NULL AND :toPrice IS NULL) 
          )
        ORDER BY p.name ASC
    """)
    Page<Product> search(
            @Param("keyword") String keyword,
            @Param("categoryName") String categoryName,
            @Param("status") ProductStatus status,
            @Param("color") ProductColor color,
            @Param("size") ProductSize size,
            @Param("fromPrice") BigDecimal fromPrice,
            @Param("toPrice") BigDecimal toPrice,
            Pageable pageable
    );
}