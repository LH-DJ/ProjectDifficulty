package com.example.productservice.entity;

import com.example.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品實體類
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "products")
public class Product extends BaseEntity {
    
    @NotBlank(message = "商品名稱不能為空")
    @Size(min = 1, max = 100, message = "商品名稱長度必須在1-100個字符之間")
    @Column(nullable = false)
    private String name;
    
    @Size(max = 500, message = "商品描述長度不能超過500個字符")
    private String description;
    
    @NotNull(message = "商品價格不能為空")
    @DecimalMin(value = "0.0", message = "商品價格不能小於0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @NotNull(message = "商品庫存不能為空")
    @Column(nullable = false)
    private Integer stock;
    
    @Size(max = 500, message = "商品圖片URL長度不能超過500個字符")
    private String imageUrl;
    
    @Size(max = 50, message = "品牌長度不能超過50個字符")
    private String brand;
    
    @Size(max = 50, message = "型號長度不能超過50個字符")
    private String model;
    
    @DecimalMin(value = "0.0", message = "重量不能小於0")
    @Column(precision = 8, scale = 2)
    private BigDecimal weight;
    
    @Size(max = 10, message = "重量單位長度不能超過10個字符")
    private String weightUnit;
    
    @Size(max = 10, message = "尺寸單位長度不能超過10個字符")
    private String dimensionUnit;
    
    @DecimalMin(value = "0.0", message = "長度不能小於0")
    @Column(precision = 8, scale = 2)
    private BigDecimal length;
    
    @DecimalMin(value = "0.0", message = "寬度不能小於0")
    @Column(precision = 8, scale = 2)
    private BigDecimal width;
    
    @DecimalMin(value = "0.0", message = "高度不能小於0")
    @Column(precision = 8, scale = 2)
    private BigDecimal height;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category = ProductCategory.OTHER;
    
    /**
     * 商品狀態枚舉
     */
    public enum ProductStatus {
        ACTIVE("啟用"),
        INACTIVE("停用"),
        OUT_OF_STOCK("缺貨"),
        DISCONTINUED("停產");
        
        private final String description;
        
        ProductStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 商品類別枚舉
     */
    public enum ProductCategory {
        ELECTRONICS("電子產品"),
        CLOTHING("服裝"),
        BOOKS("圖書"),
        HOME_AND_GARDEN("家居園藝"),
        SPORTS("運動用品"),
        BEAUTY("美妝護理"),
        AUTOMOTIVE("汽車用品"),
        OTHER("其他");
        
        private final String description;
        
        ProductCategory(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
