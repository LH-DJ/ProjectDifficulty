package com.example.userservice.repository;

import com.example.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用戶 Repository 接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根據用戶名查找用戶
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根據郵箱查找用戶
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 根據手機號查找用戶
     */
    Optional<User> findByPhone(String phone);
    
    /**
     * 檢查用戶名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 檢查郵箱是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 檢查手機號是否存在
     */
    boolean existsByPhone(String phone);
    
    /**
     * 根據狀態查找用戶
     */
    List<User> findByStatus(User.UserStatus status);
    
    /**
     * 根據角色查找用戶
     */
    List<User> findByRole(User.UserRole role);
    
    /**
     * 根據用戶名模糊查詢
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:username%")
    List<User> findByUsernameContaining(@Param("username") String username);
    
    /**
     * 根據真實姓名模糊查詢
     */
    @Query("SELECT u FROM User u WHERE u.realName LIKE %:realName%")
    List<User> findByRealNameContaining(@Param("realName") String realName);
}
