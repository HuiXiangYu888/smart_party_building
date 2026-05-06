package com.example.server.mapper;

import com.example.pojo.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskMapper {
    int insert(Task task);
    int update(Task task);
    Task selectById(@Param("id") Long id);
    int deleteById(@Param("id") Long id);

    List<Task> selectWithPagination(@Param("offset") int offset, @Param("limit") int limit);
    int selectCount();
}


