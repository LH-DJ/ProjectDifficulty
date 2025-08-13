package com.example.productservice.repository;

import com.example.productservice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 商品 Repository 接口
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * 根據商品名稱查找商品
     */
    Optional<Product> findByName(String name);
    
    /**
     * 檢查商品名稱是否存在
     */
    boolean existsByName(String name);
    
    /**
     * 根據狀態查找商品
     */
    List<Product> findByStatus(Product.ProductStatus status);
    
    /**
     * 根據分類查找商品
     */
    List<Product> findByCategory(Product.ProductCategory category);
    
    /**
     * 根據品牌查找商品
     */
    List<Product> findByBrand(String brand);
    
    /**
     * 根據價格範圍查找商品
     */
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    /**
     * 根據庫存範圍查找商品
     */
    List<Product> findByStockBetween(Integer minStock, Integer maxStock);
    
    /**
     * 根據商品名稱模糊查詢
     */
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:name%")
    List<Product> findByNameContaining(@Param("name") String name);
    
    /**
     * 根據商品描述模糊查詢
     */
    @Query("SELECT p FROM Product p WHERE p.description LIKE %:description%")
    List<Product> findByDescriptionContaining(@Param("description") String description);
    
    /**
     * 根據品牌模糊查詢
     */
    @Query("SELECT p FROM Product p WHERE p.brand LIKE %:brand%")
    List<Product> findByBrandContaining(@Param("brand") String brand);
    
    /**
     * 根據型號模糊查詢
     */
    @Query("SELECT p FROM Product p WHERE p.model LIKE %:model%")
    List<Product> findByModelContaining(@Param("model") String model);
    
    /**
     * 查找庫存不足的商品
     */
    @Query("SELECT p FROM Product p WHERE p.stock < :threshold")
    List<Product> findByStockLessThan(@Param("threshold") Integer threshold);
    
    /**
     * 查找價格低於指定值的商品
     */
    @Query("SELECT p FROM Product p WHERE p.price < :maxPrice")
    List<Product> findByPriceLessThan(@Param("maxPrice") BigDecimal maxPrice);
    
    /**
     * 查找價格高於指定值的商品
     */
    @Query("SELECT p FROM Product p WHERE p.price > :minPrice")
    List<Product> findByPriceGreaterThan(@Param("minPrice") BigDecimal minPrice);
    
    /**
     * 根據狀態和分類查找商品
     */
    List<Product> findByStatusAndCategory(Product.ProductStatus status, Product.ProductCategory category);
    
    /**
     * 根據狀態和品牌查找商品
     */
    List<Product> findByStatusAndBrand(Product.ProductStatus status, String brand);
    
    /**
     * 根據狀態和價格範圍查找商品
     */
    @Query("SELECT p FROM Product p WHERE p.status = :status AND p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findByStatusAndPriceBetween(@Param("status") Product.ProductStatus status,
                                            @Param("minPrice") BigDecimal minPrice,
                                            @Param("maxPrice") BigDecimal maxPrice);
}
