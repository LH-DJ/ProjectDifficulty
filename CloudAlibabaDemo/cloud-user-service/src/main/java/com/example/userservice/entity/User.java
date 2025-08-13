package com.example.userservice.entity;

import com.example.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用戶實體類
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    
    @NotBlank(message = "用戶名不能為空")
    @Size(min = 3, max = 20, message = "用戶名長度必須在3-20個字符之間")
    @Column(unique = true, nullable = false)
    private String username;
    
    @NotBlank(message = "密碼不能為空")
    @Size(min = 6, max = 100, message = "密碼長度必須在6-100個字符之間")
    @Column(nullable = false)
    private String password;
    
    @NotBlank(message = "郵箱不能為空")
    @Email(message = "郵箱格式不正確")
    @Column(unique = true, nullable = false)
    private String email;
    
    @Size(max = 50, message = "真實姓名長度不能超過50個字符")
    private String realName;
    
    @Size(max = 20, message = "手機號長度不能超過20個字符")
    private String phone;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;
    
    /**
     * 用戶狀態枚舉
     */
    public enum UserStatus {
        ACTIVE("啟用"),
        INACTIVE("禁用"),
        LOCKED("鎖定");
        
        private final String description;
        
        UserStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 用戶角色枚舉
     */
    public enum UserRole {
        USER("普通用戶"),
        ADMIN("管理員"),
        VIP("VIP用戶");
        
        private final String description;
        
        UserRole(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
