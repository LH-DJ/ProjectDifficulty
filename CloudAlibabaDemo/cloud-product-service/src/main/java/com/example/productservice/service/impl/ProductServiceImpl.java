package com.example.productservice.service.impl;

import com.example.common.exception.BusinessException;
import com.example.common.result.ResultCode;
import com.example.productservice.dto.ProductDTO;
import com.example.productservice.entity.Product;
import com.example.productservice.entity.Product.ProductCategory;
import com.example.productservice.entity.Product.ProductStatus;
import com.example.productservice.repository.ProductRepository;
import com.example.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Product createProduct(ProductDTO productDTO) {
        log.info("創建商品: {}", productDTO.getName());
        
        // 檢查商品名稱是否已存在
        if (isProductNameExists(productDTO.getName())) {
            throw new BusinessException(ResultCode.PRODUCT_ALREADY_EXISTS);
        }

        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setStock(productDTO.getStock());
        product.setImageUrl(productDTO.getImageUrl());
        product.setBrand(productDTO.getBrand());
        product.setModel(productDTO.getModel());
        product.setWeight(productDTO.getWeight());
        product.setWeightUnit(productDTO.getWeightUnit());
        product.setDimensionUnit(productDTO.getDimensionUnit());
        product.setLength(productDTO.getLength());
        product.setWidth(productDTO.getWidth());
        product.setHeight(productDTO.getHeight());
        product.setStatus(ProductStatus.ACTIVE);
        product.setCategory(productDTO.getCategory());

        Product savedProduct = productRepository.save(product);
        log.info("商品創建成功，ID: {}", savedProduct.getId());
        return savedProduct;
    }

    @Override
    public Product updateProduct(Long id, ProductDTO productDTO) {
        log.info("更新商品，ID: {}", id);
        
        Product existingProduct = findById(id);
        if (existingProduct == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }

        // 檢查名稱是否重複（排除當前商品）
        if (!existingProduct.getName().equals(productDTO.getName()) && 
            isProductNameExists(productDTO.getName())) {
            throw new BusinessException(ResultCode.PRODUCT_ALREADY_EXISTS);
        }

        existingProduct.setName(productDTO.getName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setStock(productDTO.getStock());
        existingProduct.setImageUrl(productDTO.getImageUrl());
        existingProduct.setBrand(productDTO.getBrand());
        existingProduct.setModel(productDTO.getModel());
        existingProduct.setWeight(productDTO.getWeight());
        existingProduct.setWeightUnit(productDTO.getWeightUnit());
        existingProduct.setDimensionUnit(productDTO.getDimensionUnit());
        existingProduct.setLength(productDTO.getLength());
        existingProduct.setWidth(productDTO.getWidth());
        existingProduct.setHeight(productDTO.getHeight());
        existingProduct.setCategory(productDTO.getCategory());

        Product updatedProduct = productRepository.save(existingProduct);
        log.info("商品更新成功，ID: {}", updatedProduct.getId());
        return updatedProduct;
    }

    @Override
    public Product findById(Long id) {
        log.debug("根據ID查找商品: {}", id);
        Optional<Product> product = productRepository.findById(id);
        return product.orElse(null);
    }

    @Override
    public Product findByName(String name) {
        log.debug("根據名稱查找商品: {}", name);
        return productRepository.findByName(name);
    }

    @Override
    public List<Product> findAllProducts() {
        log.debug("查找所有商品");
        return productRepository.findAll();
    }

    @Override
    public Page<Product> findProductsByPage(int page, int size) {
        log.debug("分頁查找商品，頁碼: {}, 大小: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return productRepository.findAll(pageable);
    }

    @Override
    public List<Product> findProductsByStatus(ProductStatus status) {
        log.debug("根據狀態查找商品: {}", status);
        return productRepository.findByStatus(status);
    }

    @Override
    public List<Product> findProductsByCategory(ProductCategory category) {
        log.debug("根據類別查找商品: {}", category);
        return productRepository.findByCategory(category);
    }

    @Override
    public List<Product> findProductsByBrand(String brand) {
        log.debug("根據品牌查找商品: {}", brand);
        return productRepository.findByBrand(brand);
    }

    @Override
    public List<Product> findProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("根據價格範圍查找商品: {} - {}", minPrice, maxPrice);
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    @Override
    public List<Product> findProductsByStockRange(Integer minStock, Integer maxStock) {
        log.debug("根據庫存範圍查找商品: {} - {}", minStock, maxStock);
        return productRepository.findByStockBetween(minStock, maxStock);
    }

    @Override
    public List<Product> findProductsByNameContaining(String keyword) {
        log.debug("根據名稱關鍵字查找商品: {}", keyword);
        return productRepository.findByNameContaining(keyword);
    }

    @Override
    public List<Product> findProductsByDescriptionContaining(String keyword) {
        log.debug("根據描述關鍵字查找商品: {}", keyword);
        return productRepository.findByDescriptionContaining(keyword);
    }

    @Override
    public List<Product> findProductsByBrandContaining(String keyword) {
        log.debug("根據品牌關鍵字查找商品: {}", keyword);
        return productRepository.findByBrandContaining(keyword);
    }

    @Override
    public List<Product> findProductsByModelContaining(String keyword) {
        log.debug("根據型號關鍵字查找商品: {}", keyword);
        return productRepository.findByModelContaining(keyword);
    }

    @Override
    public List<Product> findProductsWithLowStock(Integer threshold) {
        log.debug("查找低庫存商品，閾值: {}", threshold);
        return productRepository.findByStockLessThan(threshold);
    }

    @Override
    public List<Product> findProductsBelowPrice(BigDecimal price) {
        log.debug("查找低於價格的商品: {}", price);
        return productRepository.findByPriceLessThan(price);
    }

    @Override
    public List<Product> findProductsAbovePrice(BigDecimal price) {
        log.debug("查找高於價格的商品: {}", price);
        return productRepository.findByPriceGreaterThan(price);
    }

    @Override
    public List<Product> findProductsByStatusAndCategory(ProductStatus status, ProductCategory category) {
        log.debug("根據狀態和類別查找商品: {}, {}", status, category);
        return productRepository.findByStatusAndCategory(status, category);
    }

    @Override
    public List<Product> findProductsByStatusAndBrand(ProductStatus status, String brand) {
        log.debug("根據狀態和品牌查找商品: {}, {}", status, brand);
        return productRepository.findByStatusAndBrand(status, brand);
    }

    @Override
    public List<Product> findProductsByStatusAndPriceRange(ProductStatus status, BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("根據狀態和價格範圍查找商品: {}, {} - {}", status, minPrice, maxPrice);
        return productRepository.findByStatusAndPriceRange(status, minPrice, maxPrice);
    }

    @Override
    public void deleteProduct(Long id) {
        log.info("刪除商品，ID: {}", id);
        Product product = findById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }
        productRepository.deleteById(id);
        log.info("商品刪除成功，ID: {}", id);
    }

    @Override
    public Product activateProduct(Long id) {
        log.info("啟用商品，ID: {}", id);
        Product product = findById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }
        product.setStatus(ProductStatus.ACTIVE);
        Product activatedProduct = productRepository.save(product);
        log.info("商品啟用成功，ID: {}", id);
        return activatedProduct;
    }

    @Override
    public Product deactivateProduct(Long id) {
        log.info("停用商品，ID: {}", id);
        Product product = findById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }
        product.setStatus(ProductStatus.INACTIVE);
        Product deactivatedProduct = productRepository.save(product);
        log.info("商品停用成功，ID: {}", id);
        return deactivatedProduct;
    }

    @Override
    public Product setProductOutOfStock(Long id) {
        log.info("設置商品缺貨，ID: {}", id);
        Product product = findById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }
        product.setStatus(ProductStatus.OUT_OF_STOCK);
        Product outOfStockProduct = productRepository.save(product);
        log.info("商品設置為缺貨成功，ID: {}", id);
        return outOfStockProduct;
    }

    @Override
    public Product setProductDiscontinued(Long id) {
        log.info("設置商品停產，ID: {}", id);
        Product product = findById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }
        product.setStatus(ProductStatus.DISCONTINUED);
        Product discontinuedProduct = productRepository.save(product);
        log.info("商品設置為停產成功，ID: {}", id);
        return discontinuedProduct;
    }

    @Override
    public Product updateProductStock(Long id, Integer newStock) {
        log.info("更新商品庫存，ID: {}, 新庫存: {}", id, newStock);
        Product product = findById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }
        if (newStock < 0) {
            throw new BusinessException(ResultCode.INVALID_STOCK_AMOUNT);
        }
        
        product.setStock(newStock);
        
        // 根據庫存狀態自動更新商品狀態
        if (newStock == 0) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        } else if (product.getStatus() == ProductStatus.OUT_OF_STOCK) {
            product.setStatus(ProductStatus.ACTIVE);
        }
        
        Product updatedProduct = productRepository.save(product);
        log.info("商品庫存更新成功，ID: {}, 新庫存: {}", id, newStock);
        return updatedProduct;
    }

    @Override
    public Product decreaseProductStock(Long id, Integer amount) {
        log.info("減少商品庫存，ID: {}, 減少數量: {}", id, amount);
        Product product = findById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }
        if (amount <= 0) {
            throw new BusinessException(ResultCode.INVALID_STOCK_AMOUNT);
        }
        if (product.getStock() < amount) {
            throw new BusinessException(ResultCode.INSUFFICIENT_STOCK);
        }
        
        product.setStock(product.getStock() - amount);
        
        // 檢查是否需要更新狀態
        if (product.getStock() == 0) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        }
        
        Product updatedProduct = productRepository.save(product);
        log.info("商品庫存減少成功，ID: {}, 剩餘庫存: {}", id, updatedProduct.getStock());
        return updatedProduct;
    }

    @Override
    public Product increaseProductStock(Long id, Integer amount) {
        log.info("增加商品庫存，ID: {}, 增加數量: {}", id, amount);
        Product product = findById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }
        if (amount <= 0) {
            throw new BusinessException(ResultCode.INVALID_STOCK_AMOUNT);
        }
        
        product.setStock(product.getStock() + amount);
        
        // 如果之前是缺貨狀態，現在有庫存了，改為啟用狀態
        if (product.getStatus() == ProductStatus.OUT_OF_STOCK) {
            product.setStatus(ProductStatus.ACTIVE);
        }
        
        Product updatedProduct = productRepository.save(product);
        log.info("商品庫存增加成功，ID: {}, 新庫存: {}", id, updatedProduct.getStock());
        return updatedProduct;
    }

    @Override
    public boolean isStockSufficient(Long id, Integer requiredAmount) {
        log.debug("檢查商品庫存是否充足，ID: {}, 需要數量: {}", id, requiredAmount);
        Product product = findById(id);
        if (product == null) {
            return false;
        }
        return product.getStock() >= requiredAmount;
    }

    @Override
    public boolean isProductNameExists(String name) {
        log.debug("檢查商品名稱是否存在: {}", name);
        return productRepository.existsByName(name);
    }
}
