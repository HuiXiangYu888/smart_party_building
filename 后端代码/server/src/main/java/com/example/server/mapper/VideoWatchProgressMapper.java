package com.example.server.mapper;

import com.example.pojo.entity.VideoWatchProgress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VideoWatchProgressMapper {
    
    /**
     * 插入或更新观看进度
     */
    int insertOrUpdate(VideoWatchProgress progress);
    
    /**
     * 根据用户ID和资源ID查询观看进度
     */
    VideoWatchProgress selectByUserIdAndResourceId(@Param("userId") Long userId, @Param("resourceId") Long resourceId);
    
    /**
     * 更新观看进度
     */
    int updateProgress(VideoWatchProgress progress);
    
    /**
     * 删除观看进度
     */
    int deleteByUserIdAndResourceId(@Param("userId") Long userId, @Param("resourceId") Long resourceId);
}
