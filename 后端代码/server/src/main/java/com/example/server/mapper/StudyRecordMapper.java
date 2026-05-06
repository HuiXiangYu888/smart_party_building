package com.example.server.mapper;

import com.example.pojo.entity.StudyRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface StudyRecordMapper {
    
    /**
     * 插入学习记录
     */
    int insert(StudyRecord record);
    
    /**
     * 根据用户ID查询学习记录
     */
    List<StudyRecord> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID查询学习记录（带视频标题）
     */
    List<Map<String, Object>> selectByUserIdWithTitle(@Param("userId") Long userId);
    
    /**
     * 根据用户ID和资源ID查询学习记录
     */
    List<StudyRecord> selectByUserIdAndResourceId(@Param("userId") Long userId, @Param("resourceId") Long resourceId);
    
    /**
     * 查询用户总学习时长
     */
    Integer selectTotalDurationByUserId(@Param("userId") Long userId);
    
    /**
     * 查询所有用户的学习统计
     */
    List<Map<String, Object>> selectAllUserStudyStats();
    
    /**
     * 根据姓名或学号搜索用户学习统计
     */
    List<Map<String, Object>> selectUserStudyStatsByKeyword(@Param("keyword") String keyword);
    
    /**
     * 清空用户学习记录
     */
    int deleteByUserId(@Param("userId") Long userId);
    
    /**
     * 清空所有学习记录
     */
    int deleteAll();
}
