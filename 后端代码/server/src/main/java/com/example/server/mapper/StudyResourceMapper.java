package com.example.server.mapper;

import com.example.pojo.entity.StudyResource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StudyResourceMapper {
    int insert(StudyResource r);
    int deleteById(@Param("id") Long id);
    StudyResource selectById(@Param("id") Long id);
    List<StudyResource> selectAll(@Param("type") String type);
    int deleteAll();
}


