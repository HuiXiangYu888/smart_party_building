package com.example.server.mapper;

import com.example.pojo.entity.TaskEvaluation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskEvaluationMapper {
    int insert(TaskEvaluation evaluation);
    List<TaskEvaluation> selectByTaskId(@Param("taskId") Long taskId);
}


