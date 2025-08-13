package com.example.userservice.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用戶 DTO 類
 */
@Data
public class UserDTO {
    
    private Long id;
    
    @NotBlank(message = "用戶名不能為空")
    @Size(min = 3, max = 20, message = "用戶名長度必須在3-20個字符之間")
    private String username;
    
    @NotBlank(message = "密碼不能為空")
    @Size(min = 6, max = 100, message = "密碼長度必須在6-100個字符之間")
    private String password;
    
    @NotBlank(message = "郵箱不能為空")
    @Email(message = "郵箱格式不正確")
    private String email;
    
    @Size(max = 50, message = "真實姓名長度不能超過50個字符")
    private String realName;
    
    @Size(max = 20, message = "手機號長度不能超過20個字符")
    private String phone;
}
