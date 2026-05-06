package com.example.server.mapper;

import com.example.pojo.entity.ApplicationProcess;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ApplicationProcessMapper {

	ApplicationProcess selectById(@Param("id") Long id);

	ApplicationProcess selectByUserAndStage(@Param("userId") Long userId, @Param("stage") String stage);

	int insert(ApplicationProcess entity);

	int update(ApplicationProcess entity);
}


