package com.multilevelcache.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.multilevelcache.entity.Blacklist;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 黑名單 Mapper 接口
 */
public interface BlacklistMapper extends BaseMapper<Blacklist> {

    /**
     * 根據用戶ID查詢黑名單記錄
     */
    List<Blacklist> selectByUserId(@Param("userId") String userId);

    /**
     * 根據賬戶號碼查詢黑名單記錄
     */
    List<Blacklist> selectByAccountNumber(@Param("accountNumber") String accountNumber);

    /**
     * 檢查用戶是否在黑名單中
     */
    Blacklist isUserBlacklisted(@Param("userId") String userId);

    /**
     * 檢查賬戶是否在黑名單中
     */
    Blacklist isAccountBlacklisted(@Param("accountNumber") String accountNumber);

    /**
     * 根據黑名單類型查詢
     */
    List<Blacklist> selectByBlacklistType(@Param("blacklistType") String blacklistType);

    /**
     * 根據狀態查詢黑名單記錄
     */
    List<Blacklist> selectByStatus(@Param("status") String status);

    /**
     * 查詢有效的黑名單記錄（未過期且狀態為激活）
     */
    List<Blacklist> selectActiveBlacklists();

    /**
     * 統計用戶黑名單數量
     */
    Long countByUserId(@Param("userId") String userId);

    /**
     * 統計賬戶黑名單數量
     */
    Long countByAccountNumber(@Param("accountNumber") String accountNumber);
} 