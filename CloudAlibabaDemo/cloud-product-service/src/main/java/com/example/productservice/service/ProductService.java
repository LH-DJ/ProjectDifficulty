package com.example.productservice.service;

import com.example.productservice.dto.ProductDTO;
import com.example.productservice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 商品服務接口
 */
public interface ProductService {
    
    /**
     * 創建商品
     */
    Product createProduct(ProductDTO productDTO);
    
    /**
     * 更新商品
     */
    Product updateProduct(Long id, ProductDTO productDTO);
    
    /**
     * 根據ID查找商品
     */
    Optional<Product> findById(Long id);
    
    /**
     * 根據商品名稱查找商品
     */
    Optional<Product> findByName(String name);
    
    /**
     * 獲取所有商品
     */
    List<Product> findAllProducts();
    
    /**
     * 分頁查詢商品
     */
    Page<Product> findProductsByPage(Pageable pageable);
    
    /**
     * 根據狀態查找商品
     */
    List<Product> findProductsByStatus(Product.ProductStatus status);
    
    /**
     * 根據分類查找商品
     */
    List<Product> findProductsByCategory(Product.ProductCategory category);
    
    /**
     * 根據品牌查找商品
     */
    List<Product> findProductsByBrand(String brand);
    
    /**
     * 根據價格範圍查找商品
     */
    List<Product> findProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
    
    /**
     * 根據庫存範圍查找商品
     */
    List<Product> findProductsByStockRange(Integer minStock, Integer maxStock);
    
    /**
     * 根據商品名稱模糊查詢
     */
    List<Product> findProductsByNameContaining(String name);
    
    /**
     * 根據商品描述模糊查詢
     */
    List<Product> findProductsByDescriptionContaining(String description);
    
    /**
     * 根據品牌模糊查詢
     */
    List<Product> findProductsByBrandContaining(String brand);
    
    /**
     * 根據型號模糊查詢
     */
    List<Product> findProductsByModelContaining(String model);
    
    /**
     * 查找庫存不足的商品
     */
    List<Product> findProductsWithLowStock(Integer threshold);
    
    /**
     * 查找價格低於指定值的商品
     */
    List<Product> findProductsBelowPrice(BigDecimal maxPrice);
    
    /**
     * 查找價格高於指定值的商品
     */
    List<Product> findProductsAbovePrice(BigDecimal minPrice);
    
    /**
     * 根據狀態和分類查找商品
     */
    List<Product> findProductsByStatusAndCategory(Product.ProductStatus status, Product.ProductCategory category);
    
    /**
     * 根據狀態和品牌查找商品
     */
    List<Product> findProductsByStatusAndBrand(Product.ProductStatus status, String brand);
    
    /**
     * 根據狀態和價格範圍查找商品
     */
    List<Product> findProductsByStatusAndPriceRange(Product.ProductStatus status, BigDecimal minPrice, BigDecimal maxPrice);
    
    /**
     * 刪除商品
     */
    void deleteProduct(Long id);
    
    /**
     * 上架商品
     */
    Product activateProduct(Long id);
    
    /**
     * 下架商品
     */
    Product deactivateProduct(Long id);
    
    /**
     * 設置商品缺貨
     */
    Product setProductOutOfStock(Long id);
    
    /**
     * 設置商品停產
     */
    Product setProductDiscontinued(Long id);
    
    /**
     * 更新商品庫存
     */
    Product updateProductStock(Long id, Integer newStock);
    
    /**
     * 減少商品庫存
     */
    Product decreaseProductStock(Long id, Integer quantity);
    
    /**
     * 增加商品庫存
     */
    Product increaseProductStock(Long id, Integer quantity);
    
    /**
     * 檢查商品庫存是否充足
     */
    boolean isStockSufficient(Long id, Integer quantity);
    
    /**
     * 檢查商品名稱是否存在
     */
    boolean isProductNameExists(String name);
}
