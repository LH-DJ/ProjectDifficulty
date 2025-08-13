package com.example.productservice.dto;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 商品 DTO 類
 */
@Data
public class ProductDTO {
    
    private Long id;
    
    @NotBlank(message = "商品名稱不能為空")
    @Size(max = 100, message = "商品名稱長度不能超過100個字符")
    private String name;
    
    @Size(max = 500, message = "商品描述長度不能超過500個字符")
    private String description;
    
    @NotNull(message = "商品價格不能為空")
    @DecimalMin(value = "0.01", message = "商品價格必須大於0")
    private BigDecimal price;
    
    @NotNull(message = "商品庫存不能為空")
    private Integer stock;
    
    @Size(max = 200, message = "商品圖片URL長度不能超過200個字符")
    private String imageUrl;
    
    @Size(max = 50, message = "品牌名稱長度不能超過50個字符")
    private String brand;
    
    @Size(max = 50, message = "型號長度不能超過50個字符")
    private String model;
    
    private BigDecimal weight;
    
    @Size(max = 20, message = "重量單位長度不能超過20個字符")
    private String weightUnit;
    
    @Size(max = 20, message = "尺寸單位長度不能超過20個字符")
    private String dimensionUnit;
    
    private BigDecimal length;
    
    private BigDecimal width;
    
    private BigDecimal height;
}
