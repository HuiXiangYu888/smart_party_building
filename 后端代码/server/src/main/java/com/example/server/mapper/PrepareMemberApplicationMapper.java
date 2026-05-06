package com.example.server.mapper;

import com.example.pojo.entity.PrepareMemberApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PrepareMemberApplicationMapper {

	PrepareMemberApplication selectById(@Param("id") Long id);

	List<PrepareMemberApplication> selectByUserId(@Param("userId") Long userId);

	List<PrepareMemberApplication> selectAll();

	int insert(PrepareMemberApplication entity);

	int update(PrepareMemberApplication entity);
}


