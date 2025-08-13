package com.example.userservice.controller;

import com.example.common.result.Result;
import com.example.userservice.dto.UserDTO;
import com.example.userservice.entity.User;
import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Optional;

/**
 * 用戶控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    
    private final UserService userService;
    
    /**
     * 創建用戶
     */
    @PostMapping
    public Result<User> createUser(@Valid @RequestBody UserDTO userDTO) {
        log.info("創建用戶請求：{}", userDTO.getUsername());
        User user = userService.createUser(userDTO);
        return Result.success(user);
    }
    
    /**
     * 更新用戶
     */
    @PutMapping("/{id}")
    public Result<User> updateUser(@PathVariable @Min(1) Long id, 
                                   @Valid @RequestBody UserDTO userDTO) {
        log.info("更新用戶請求：ID={}", id);
        User user = userService.updateUser(id, userDTO);
        return Result.success(user);
    }
    
    /**
     * 根據ID查找用戶
     */
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable @Min(1) Long id) {
        log.info("查詢用戶請求：ID={}", id);
        Optional<User> userOpt = userService.findById(id);
        
        if (userOpt.isPresent()) {
            return Result.success(userOpt.get());
        } else {
            return Result.failed("用戶不存在");
        }
    }
    
    /**
     * 根據用戶名查找用戶
     */
    @GetMapping("/username/{username}")
    public Result<User> getUserByUsername(@PathVariable @NotBlank String username) {
        log.info("根據用戶名查詢用戶：{}", username);
        Optional<User> userOpt = userService.findByUsername(username);
        
        if (userOpt.isPresent()) {
            return Result.success(userOpt.get());
        } else {
            return Result.failed("用戶不存在");
        }
    }
    
    /**
     * 根據郵箱查找用戶
     */
    @GetMapping("/email/{email}")
    public Result<User> getUserByEmail(@PathVariable @NotBlank String email) {
        log.info("根據郵箱查詢用戶：{}", email);
        Optional<User> userOpt = userService.findByEmail(email);
        
        if (userOpt.isPresent()) {
            return Result.success(userOpt.get());
        } else {
            return Result.failed("用戶不存在");
        }
    }
    
    /**
     * 獲取所有用戶
     */
    @GetMapping
    public Result<List<User>> getAllUsers() {
        log.info("獲取所有用戶請求");
        List<User> users = userService.findAllUsers();
        return Result.success(users);
    }
    
    /**
     * 分頁查詢用戶
     */
    @GetMapping("/page")
    public Result<Page<User>> getUsersByPage(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("分頁查詢用戶：page={}, size={}, sortBy={}, sortDir={}", page, size, sortBy, sortDir);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<User> users = userService.findUsersByPage(pageable);
        return Result.success(users);
    }
    
    /**
     * 根據狀態查找用戶
     */
    @GetMapping("/status/{status}")
    public Result<List<User>> getUsersByStatus(@PathVariable String status) {
        log.info("根據狀態查詢用戶：{}", status);
        
        try {
            User.UserStatus userStatus = User.UserStatus.valueOf(status.toUpperCase());
            List<User> users = userService.findUsersByStatus(userStatus);
            return Result.success(users);
        } catch (IllegalArgumentException e) {
            return Result.failed("無效的狀態值");
        }
    }
    
    /**
     * 根據角色查找用戶
     */
    @GetMapping("/role/{role}")
    public Result<List<User>> getUsersByRole(@PathVariable String role) {
        log.info("根據角色查詢用戶：{}", role);
        
        try {
            User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
            List<User> users = userService.findUsersByRole(userRole);
            return Result.success(users);
        } catch (IllegalArgumentException e) {
            return Result.failed("無效的角色值");
        }
    }
    
    /**
     * 根據用戶名模糊查詢
     */
    @GetMapping("/search/username")
    public Result<List<User>> searchUsersByUsername(@RequestParam @NotBlank String username) {
        log.info("根據用戶名模糊查詢：{}", username);
        List<User> users = userService.findUsersByUsernameContaining(username);
        return Result.success(users);
    }
    
    /**
     * 根據真實姓名模糊查詢
     */
    @GetMapping("/search/realname")
    public Result<List<User>> searchUsersByRealName(@RequestParam @NotBlank String realName) {
        log.info("根據真實姓名模糊查詢：{}", realName);
        List<User> users = userService.findUsersByRealNameContaining(realName);
        return Result.success(users);
    }
    
    /**
     * 刪除用戶
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable @Min(1) Long id) {
        log.info("刪除用戶請求：ID={}", id);
        userService.deleteUser(id);
        return Result.success();
    }
    
    /**
     * 啟用用戶
     */
    @PutMapping("/{id}/activate")
    public Result<User> activateUser(@PathVariable @Min(1) Long id) {
        log.info("啟用用戶請求：ID={}", id);
        User user = userService.activateUser(id);
        return Result.success(user);
    }
    
    /**
     * 禁用用戶
     */
    @PutMapping("/{id}/deactivate")
    public Result<User> deactivateUser(@PathVariable @Min(1) Long id) {
        log.info("禁用用戶請求：ID={}", id);
        User user = userService.deactivateUser(id);
        return Result.success(user);
    }
    
    /**
     * 鎖定用戶
     */
    @PutMapping("/{id}/lock")
    public Result<User> lockUser(@PathVariable @Min(1) Long id) {
        log.info("鎖定用戶請求：ID={}", id);
        User user = userService.lockUser(id);
        return Result.success(user);
    }
    
    /**
     * 檢查用戶名是否存在
     */
    @GetMapping("/check/username/{username}")
    public Result<Boolean> checkUsernameExists(@PathVariable @NotBlank String username) {
        log.info("檢查用戶名是否存在：{}", username);
        boolean exists = userService.isUsernameExists(username);
        return Result.success(exists);
    }
    
    /**
     * 檢查郵箱是否存在
     */
    @GetMapping("/check/email/{email}")
    public Result<Boolean> checkEmailExists(@PathVariable @NotBlank String email) {
        log.info("檢查郵箱是否存在：{}", email);
        boolean exists = userService.isEmailExists(email);
        return Result.success(exists);
    }
    
    /**
     * 檢查手機號是否存在
     */
    @GetMapping("/check/phone/{phone}")
    public Result<Boolean> checkPhoneExists(@PathVariable @NotBlank String phone) {
        log.info("檢查手機號是否存在：{}", phone);
        boolean exists = userService.isPhoneExists(phone);
        return Result.success(exists);
    }
    
    /**
     * 用戶登錄
     */
    @PostMapping("/login")
    public Result<User> login(@RequestParam @NotBlank String username,
                              @RequestParam @NotBlank String password) {
        log.info("用戶登錄請求：{}", username);
        
        Optional<User> userOpt = userService.validateLogin(username, password);
        
        if (userOpt.isPresent()) {
            return Result.success(userOpt.get());
        } else {
            return Result.failed(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }
    }
}
