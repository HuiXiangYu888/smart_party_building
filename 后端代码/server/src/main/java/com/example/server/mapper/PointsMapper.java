package com.example.server.mapper;

import com.example.pojo.entity.Points;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface PointsMapper {
    List<Points> selectByUserId(@Param("userId") Long userId);
    Integer sumByUserId(@Param("userId") Long userId);
    List<Map<String, Object>> selectAllMemberPointsRank();
    int insert(Points points);
    
    // 新增：根据任务标题删除积分记录
    int deleteByTaskTitle(@Param("taskTitle") String taskTitle);
    
    // 新增：根据用户ID和任务标题删除积分记录
    int deleteByUserIdAndTaskTitle(@Param("userId") Long userId, @Param("taskTitle") String taskTitle);
    
    // 新增：根据描述删除积分记录
    int deleteByDescription(@Param("description") String description);
}


