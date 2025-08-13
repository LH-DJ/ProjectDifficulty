package com.example.userservice.service;

import com.example.userservice.dto.UserDTO;
import com.example.userservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 用戶服務接口
 */
public interface UserService {
    
    /**
     * 創建用戶
     */
    User createUser(UserDTO userDTO);
    
    /**
     * 更新用戶
     */
    User updateUser(Long id, UserDTO userDTO);
    
    /**
     * 根據ID查找用戶
     */
    Optional<User> findById(Long id);
    
    /**
     * 根據用戶名查找用戶
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根據郵箱查找用戶
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 獲取所有用戶
     */
    List<User> findAllUsers();
    
    /**
     * 分頁查詢用戶
     */
    Page<User> findUsersByPage(Pageable pageable);
    
    /**
     * 根據狀態查找用戶
     */
    List<User> findUsersByStatus(User.UserStatus status);
    
    /**
     * 根據角色查找用戶
     */
    List<User> findUsersByRole(User.UserRole role);
    
    /**
     * 根據用戶名模糊查詢
     */
    List<User> findUsersByUsernameContaining(String username);
    
    /**
     * 根據真實姓名模糊查詢
     */
    List<User> findUsersByRealNameContaining(String realName);
    
    /**
     * 刪除用戶
     */
    void deleteUser(Long id);
    
    /**
     * 啟用用戶
     */
    User activateUser(Long id);
    
    /**
     * 禁用用戶
     */
    User deactivateUser(Long id);
    
    /**
     * 鎖定用戶
     */
    User lockUser(Long id);
    
    /**
     * 檢查用戶名是否存在
     */
    boolean isUsernameExists(String username);
    
    /**
     * 檢查郵箱是否存在
     */
    boolean isEmailExists(String email);
    
    /**
     * 檢查手機號是否存在
     */
    boolean isPhoneExists(String phone);
    
    /**
     * 驗證用戶登錄
     */
    Optional<User> validateLogin(String username, String password);
}
