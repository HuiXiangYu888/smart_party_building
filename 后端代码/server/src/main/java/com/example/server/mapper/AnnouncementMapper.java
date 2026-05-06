package com.example.server.mapper;

import com.example.pojo.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AnnouncementMapper {
    
    /**
     * 插入公告
     */
    int insert(Announcement announcement);
    
    /**
     * 根据ID查询公告
     */
    Announcement selectById(@Param("id") Long id);
    
    /**
     * 查询所有公告（带关联信息）
     */
    List<Announcement> selectAllWithDetails();
    
    /**
     * 根据支部ID查询公告
     */
    List<Announcement> selectByBranchId(@Param("branchId") Long branchId);
    
    /**
     * 分页查询公告
     */
    List<Announcement> selectWithPagination(@Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 查询公告总数
     */
    int selectCount();
    
    /**
     * 更新公告
     */
    int update(Announcement announcement);
    
    /**
     * 删除公告
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 根据关键词搜索公告
     */
    List<Announcement> selectByKeyword(@Param("keyword") String keyword);
}
