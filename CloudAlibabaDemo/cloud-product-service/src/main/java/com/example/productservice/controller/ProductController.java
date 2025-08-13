package com.example.productservice.controller;

import com.example.common.result.Result;
import com.example.productservice.dto.ProductDTO;
import com.example.productservice.entity.Product;
import com.example.productservice.entity.Product.ProductCategory;
import com.example.productservice.entity.Product.ProductStatus;
import com.example.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * 創建商品
     */
    @PostMapping
    public Result<Product> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        log.info("創建商品請求: {}", productDTO.getName());
        Product product = productService.createProduct(productDTO);
        return Result.success(product);
    }

    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    public Result<Product> updateProduct(
            @PathVariable @NotNull(message = "商品ID不能為空") Long id,
            @Valid @RequestBody ProductDTO productDTO) {
        log.info("更新商品請求，ID: {}", id);
        Product product = productService.updateProduct(id, productDTO);
        return Result.success(product);
    }

    /**
     * 根據ID查找商品
     */
    @GetMapping("/{id}")
    public Result<Product> getProductById(
            @PathVariable @NotNull(message = "商品ID不能為空") Long id) {
        log.debug("查找商品請求，ID: {}", id);
        Product product = productService.findById(id);
        if (product == null) {
            return Result.failed("商品不存在");
        }
        return Result.success(product);
    }

    /**
     * 根據名稱查找商品
     */
    @GetMapping("/name/{name}")
    public Result<Product> getProductByName(
            @PathVariable @NotBlank(message = "商品名稱不能為空") String name) {
        log.debug("根據名稱查找商品請求: {}", name);
        Product product = productService.findByName(name);
        if (product == null) {
            return Result.failed("商品不存在");
        }
        return Result.success(product);
    }

    /**
     * 獲取所有商品
     */
    @GetMapping
    public Result<List<Product>> getAllProducts() {
        log.debug("獲取所有商品請求");
        List<Product> products = productService.findAllProducts();
        return Result.success(products);
    }

    /**
     * 分頁獲取商品
     */
    @GetMapping("/page")
    public Result<Page<Product>> getProductsByPage(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "頁碼不能小於0") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "頁大小不能小於1") @Max(value = 100, message = "頁大小不能大於100") int size) {
        log.debug("分頁獲取商品請求，頁碼: {}, 大小: {}", page, size);
        Page<Product> products = productService.findProductsByPage(page, size);
        return Result.success(products);
    }

    /**
     * 根據狀態查找商品
     */
    @GetMapping("/status/{status}")
    public Result<List<Product>> getProductsByStatus(
            @PathVariable @NotNull(message = "商品狀態不能為空") ProductStatus status) {
        log.debug("根據狀態查找商品請求: {}", status);
        List<Product> products = productService.findProductsByStatus(status);
        return Result.success(products);
    }

    /**
     * 根據類別查找商品
     */
    @GetMapping("/category/{category}")
    public Result<List<Product>> getProductsByCategory(
            @PathVariable @NotNull(message = "商品類別不能為空") ProductCategory category) {
        log.debug("根據類別查找商品請求: {}", category);
        List<Product> products = productService.findProductsByCategory(category);
        return Result.success(products);
    }

    /**
     * 根據品牌查找商品
     */
    @GetMapping("/brand/{brand}")
    public Result<List<Product>> getProductsByBrand(
            @PathVariable @NotBlank(message = "品牌不能為空") String brand) {
        log.debug("根據品牌查找商品請求: {}", brand);
        List<Product> products = productService.findProductsByBrand(brand);
        return Result.success(products);
    }

    /**
     * 根據價格範圍查找商品
     */
    @GetMapping("/price-range")
    public Result<List<Product>> getProductsByPriceRange(
            @RequestParam @NotNull(message = "最小價格不能為空") @DecimalMin(value = "0.0", message = "最小價格不能小於0") BigDecimal minPrice,
            @RequestParam @NotNull(message = "最大價格不能為空") @DecimalMin(value = "0.0", message = "最大價格不能小於0") BigDecimal maxPrice) {
        log.debug("根據價格範圍查找商品請求: {} - {}", minPrice, maxPrice);
        if (minPrice.compareTo(maxPrice) > 0) {
            return Result.failed("最小價格不能大於最大價格");
        }
        List<Product> products = productService.findProductsByPriceRange(minPrice, maxPrice);
        return Result.success(products);
    }

    /**
     * 根據庫存範圍查找商品
     */
    @GetMapping("/stock-range")
    public Result<List<Product>> getProductsByStockRange(
            @RequestParam @NotNull(message = "最小庫存不能為空") @Min(value = 0, message = "最小庫存不能小於0") Integer minStock,
            @RequestParam @NotNull(message = "最大庫存不能為空") @Min(value = 0, message = "最大庫存不能小於0") Integer maxStock) {
        log.debug("根據庫存範圍查找商品請求: {} - {}", minStock, maxStock);
        if (minStock > maxStock) {
            return Result.failed("最小庫存不能大於最大庫存");
        }
        List<Product> products = productService.findProductsByStockRange(minStock, maxStock);
        return Result.success(products);
    }

    /**
     * 根據名稱關鍵字查找商品
     */
    @GetMapping("/search/name")
    public Result<List<Product>> searchProductsByName(
            @RequestParam @NotBlank(message = "搜索關鍵字不能為空") String keyword) {
        log.debug("根據名稱關鍵字搜索商品請求: {}", keyword);
        List<Product> products = productService.findProductsByNameContaining(keyword);
        return Result.success(products);
    }

    /**
     * 根據描述關鍵字查找商品
     */
    @GetMapping("/search/description")
    public Result<List<Product>> searchProductsByDescription(
            @RequestParam @NotBlank(message = "搜索關鍵字不能為空") String keyword) {
        log.debug("根據描述關鍵字搜索商品請求: {}", keyword);
        List<Product> products = productService.findProductsByDescriptionContaining(keyword);
        return Result.success(products);
    }

    /**
     * 根據品牌關鍵字查找商品
     */
    @GetMapping("/search/brand")
    public Result<List<Product>> searchProductsByBrand(
            @RequestParam @NotBlank(message = "搜索關鍵字不能為空") String keyword) {
        log.debug("根據品牌關鍵字搜索商品請求: {}", keyword);
        List<Product> products = productService.findProductsByBrandContaining(keyword);
        return Result.success(products);
    }

    /**
     * 根據型號關鍵字查找商品
     */
    @GetMapping("/search/model")
    public Result<List<Product>> searchProductsByModel(
            @RequestParam @NotBlank(message = "搜索關鍵字不能為空") String keyword) {
        log.debug("根據型號關鍵字搜索商品請求: {}", keyword);
        List<Product> products = productService.findProductsByModelContaining(keyword);
        return Result.success(products);
    }

    /**
     * 查找低庫存商品
     */
    @GetMapping("/low-stock")
    public Result<List<Product>> getProductsWithLowStock(
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "閾值不能小於1") Integer threshold) {
        log.debug("查找低庫存商品請求，閾值: {}", threshold);
        List<Product> products = productService.findProductsWithLowStock(threshold);
        return Result.success(products);
    }

    /**
     * 查找低於指定價格的商品
     */
    @GetMapping("/below-price/{price}")
    public Result<List<Product>> getProductsBelowPrice(
            @PathVariable @NotNull(message = "價格不能為空") @DecimalMin(value = "0.0", message = "價格不能小於0") BigDecimal price) {
        log.debug("查找低於價格的商品請求: {}", price);
        List<Product> products = productService.findProductsBelowPrice(price);
        return Result.success(products);
    }

    /**
     * 查找高於指定價格的商品
     */
    @GetMapping("/above-price/{price}")
    public Result<List<Product>> getProductsAbovePrice(
            @PathVariable @NotNull(message = "價格不能為空") @DecimalMin(value = "0.0", message = "價格不能小於0") BigDecimal price) {
        log.debug("查找高於價格的商品請求: {}", price);
        List<Product> products = productService.findProductsAbovePrice(price);
        return Result.success(products);
    }

    /**
     * 根據狀態和類別查找商品
     */
    @GetMapping("/status/{status}/category/{category}")
    public Result<List<Product>> getProductsByStatusAndCategory(
            @PathVariable @NotNull(message = "商品狀態不能為空") ProductStatus status,
            @PathVariable @NotNull(message = "商品類別不能為空") ProductCategory category) {
        log.debug("根據狀態和類別查找商品請求: {}, {}", status, category);
        List<Product> products = productService.findProductsByStatusAndCategory(status, category);
        return Result.success(products);
    }

    /**
     * 根據狀態和品牌查找商品
     */
    @GetMapping("/status/{status}/brand/{brand}")
    public Result<List<Product>> getProductsByStatusAndBrand(
            @PathVariable @NotNull(message = "商品狀態不能為空") ProductStatus status,
            @PathVariable @NotBlank(message = "品牌不能為空") String brand) {
        log.debug("根據狀態和品牌查找商品請求: {}, {}", status, brand);
        List<Product> products = productService.findProductsByStatusAndBrand(status, brand);
        return Result.success(products);
    }

    /**
     * 根據狀態和價格範圍查找商品
     */
    @GetMapping("/status/{status}/price-range")
    public Result<List<Product>> getProductsByStatusAndPriceRange(
            @PathVariable @NotNull(message = "商品狀態不能為空") ProductStatus status,
            @RequestParam @NotNull(message = "最小價格不能為空") @DecimalMin(value = "0.0", message = "最小價格不能小於0") BigDecimal minPrice,
            @RequestParam @NotNull(message = "最大價格不能為空") @DecimalMin(value = "0.0", message = "最大價格不能小於0") BigDecimal maxPrice) {
        log.debug("根據狀態和價格範圍查找商品請求: {}, {} - {}", status, minPrice, maxPrice);
        if (minPrice.compareTo(maxPrice) > 0) {
            return Result.failed("最小價格不能大於最大價格");
        }
        List<Product> products = productService.findProductsByStatusAndPriceRange(status, minPrice, maxPrice);
        return Result.success(products);
    }

    /**
     * 刪除商品
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(
            @PathVariable @NotNull(message = "商品ID不能為空") Long id) {
        log.info("刪除商品請求，ID: {}", id);
        productService.deleteProduct(id);
        return Result.success(null);
    }

    /**
     * 啟用商品
     */
    @PutMapping("/{id}/activate")
    public Result<Product> activateProduct(
            @PathVariable @NotNull(message = "商品ID不能為空") Long id) {
        log.info("啟用商品請求，ID: {}", id);
        Product product = productService.activateProduct(id);
        return Result.success(product);
    }

    /**
     * 停用商品
     */
    @PutMapping("/{id}/deactivate")
    public Result<Product> deactivateProduct(
            @PathVariable @NotNull(message = "商品ID不能為空") Long id) {
        log.info("停用商品請求，ID: {}", id);
        Product product = productService.deactivateProduct(id);
        return Result.success(product);
    }

    /**
     * 設置商品缺貨
     */
    @PutMapping("/{id}/out-of-stock")
    public Result<Product> setProductOutOfStock(
            @PathVariable @NotNull(message = "商品ID不能為空") Long id) {
        log.info("設置商品缺貨請求，ID: {}", id);
        Product product = productService.setProductOutOfStock(id);
        return Result.success(product);
    }

    /**
     * 設置商品停產
     */
    @PutMapping("/{id}/discontinued")
    public Result<Product> setProductDiscontinued(
            @PathVariable @NotNull(message = "商品ID不能為空") Long id) {
        log.info("設置商品停產請求，ID: {}", id);
        Product product = productService.setProductDiscontinued(id);
        return Result.success(product);
    }

    /**
     * 更新商品庫存
     */
    @PutMapping("/{id}/stock")
    public Result<Product> updateProductStock(
            @PathVariable @NotNull(message = "商品ID不能為空") Long id,
            @RequestParam @NotNull(message = "新庫存不能為空") @Min(value = 0, message = "庫存不能小於0") Integer newStock) {
        log.info("更新商品庫存請求，ID: {}, 新庫存: {}", id, newStock);
        Product product = productService.updateProductStock(id, newStock);
        return Result.success(product);
    }

    /**
     * 減少商品庫存
     */
    @PutMapping("/{id}/stock/decrease")
    public Result<Product> decreaseProductStock(
            @PathVariable @NotNull(message = "商品ID不能為空") Long id,
            @RequestParam @NotNull(message = "減少數量不能為空") @Min(value = 1, message = "減少數量不能小於1") Integer amount) {
        log.info("減少商品庫存請求，ID: {}, 減少數量: {}", id, amount);
        Product product = productService.decreaseProductStock(id, amount);
        return Result.success(product);
    }

    /**
     * 增加商品庫存
     */
    @PutMapping("/{id}/stock/increase")
    public Result<Product> increaseProductStock(
            @PathVariable @NotNull(message = "商品ID不能為空") Long id,
            @RequestParam @NotNull(message = "增加數量不能為空") @Min(value = 1, message = "增加數量不能小於1") Integer amount) {
        log.info("增加商品庫存請求，ID: {}, 增加數量: {}", id, amount);
        Product product = productService.increaseProductStock(id, amount);
        return Result.success(product);
    }

    /**
     * 檢查商品庫存是否充足
     */
    @GetMapping("/{id}/stock/check")
    public Result<Boolean> checkStockSufficient(
            @PathVariable @NotNull(message = "商品ID不能為空") Long id,
            @RequestParam @NotNull(message = "需要數量不能為空") @Min(value = 1, message = "需要數量不能小於1") Integer requiredAmount) {
        log.debug("檢查商品庫存是否充足請求，ID: {}, 需要數量: {}", id, requiredAmount);
        boolean isSufficient = productService.isStockSufficient(id, requiredAmount);
        return Result.success(isSufficient);
    }

    /**
     * 檢查商品名稱是否存在
     */
    @GetMapping("/check-name-exists")
    public Result<Boolean> checkProductNameExists(
            @RequestParam @NotBlank(message = "商品名稱不能為空") String name) {
        log.debug("檢查商品名稱是否存在請求: {}", name);
        boolean exists = productService.isProductNameExists(name);
        return Result.success(exists);
    }
}
