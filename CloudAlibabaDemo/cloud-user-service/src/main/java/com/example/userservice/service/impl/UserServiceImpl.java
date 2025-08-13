package com.example.userservice.service.impl;

import com.example.common.exception.BusinessException;
import com.example.common.result.ResultCode;
import com.example.userservice.dto.UserDTO;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * 用戶服務實現類
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public User createUser(UserDTO userDTO) {
        // 檢查用戶名是否已存在
        if (isUsernameExists(userDTO.getUsername())) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXIST);
        }
        
        // 檢查郵箱是否已存在
        if (isEmailExists(userDTO.getEmail())) {
            throw new BusinessException("郵箱已存在");
        }
        
        // 檢查手機號是否已存在
        if (StringUtils.hasText(userDTO.getPhone()) && isPhoneExists(userDTO.getPhone())) {
            throw new BusinessException("手機號已存在");
        }
        
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        
        // 加密密碼
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        
        User savedUser = userRepository.save(user);
        log.info("創建用戶成功：{}", savedUser.getUsername());
        
        return savedUser;
    }
    
    @Override
    public User updateUser(Long id, UserDTO userDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_EXIST));
        
        // 檢查用戶名是否已被其他用戶使用
        if (!existingUser.getUsername().equals(userDTO.getUsername()) && 
            isUsernameExists(userDTO.getUsername())) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXIST);
        }
        
        // 檢查郵箱是否已被其他用戶使用
        if (!existingUser.getEmail().equals(userDTO.getEmail()) && 
            isEmailExists(userDTO.getEmail())) {
            throw new BusinessException("郵箱已存在");
        }
        
        // 檢查手機號是否已被其他用戶使用
        if (StringUtils.hasText(userDTO.getPhone()) && 
            !userDTO.getPhone().equals(existingUser.getPhone()) && 
            isPhoneExists(userDTO.getPhone())) {
            throw new BusinessException("手機號已存在");
        }
        
        // 更新用戶信息
        existingUser.setUsername(userDTO.getUsername());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setRealName(userDTO.getRealName());
        existingUser.setPhone(userDTO.getPhone());
        
        // 如果提供了新密碼，則加密更新
        if (StringUtils.hasText(userDTO.getPassword())) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        
        User updatedUser = userRepository.save(existingUser);
        log.info("更新用戶成功：{}", updatedUser.getUsername());
        
        return updatedUser;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<User> findUsersByPage(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> findUsersByStatus(User.UserStatus status) {
        return userRepository.findByStatus(status);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> findUsersByRole(User.UserRole role) {
        return userRepository.findByRole(role);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> findUsersByUsernameContaining(String username) {
        return userRepository.findByUsernameContaining(username);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> findUsersByRealNameContaining(String realName) {
        return userRepository.findByRealNameContaining(realName);
    }
    
    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_EXIST));
        
        userRepository.delete(user);
        log.info("刪除用戶成功：{}", user.getUsername());
    }
    
    @Override
    public User activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_EXIST));
        
        user.setStatus(User.UserStatus.ACTIVE);
        User activatedUser = userRepository.save(user);
        log.info("啟用用戶成功：{}", activatedUser.getUsername());
        
        return activatedUser;
    }
    
    @Override
    public User deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_EXIST));
        
        user.setStatus(User.UserStatus.INACTIVE);
        User deactivatedUser = userRepository.save(user);
        log.info("禁用用戶成功：{}", deactivatedUser.getUsername());
        
        return deactivatedUser;
    }
    
    @Override
    public User lockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_EXIST));
        
        user.setStatus(User.UserStatus.LOCKED);
        User lockedUser = userRepository.save(user);
        log.info("鎖定用戶成功：{}", lockedUser.getUsername());
        
        return lockedUser;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isPhoneExists(String phone) {
        return userRepository.existsByPhone(phone);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> validateLogin(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // 檢查用戶狀態
            if (user.getStatus() != User.UserStatus.ACTIVE) {
                log.warn("用戶登錄失敗，用戶狀態異常：{}", username);
                return Optional.empty();
            }
            
            // 驗證密碼
            if (passwordEncoder.matches(password, user.getPassword())) {
                log.info("用戶登錄成功：{}", username);
                return Optional.of(user);
            } else {
                log.warn("用戶登錄失敗，密碼錯誤：{}", username);
            }
        }
        
        return Optional.empty();
    }
}
