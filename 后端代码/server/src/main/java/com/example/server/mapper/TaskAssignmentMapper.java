package com.example.server.mapper;

import com.example.pojo.entity.TaskAssignment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskAssignmentMapper {
    int insert(TaskAssignment assignment);
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    List<TaskAssignment> selectByTaskId(@Param("taskId") Long taskId);
    int deleteByTaskId(@Param("taskId") Long taskId);
    int deleteByTaskIdAndUserId(@Param("taskId") Long taskId, @Param("userId") Long userId);
    java.util.List<java.util.Map<String, Object>> selectTasksByUserId(@Param("userId") Long userId);
    int countByTaskId(@Param("taskId") Long taskId);
    java.util.List<java.util.Map<String, Object>> selectParticipantsByTaskId(@Param("taskId") Long taskId);
}


