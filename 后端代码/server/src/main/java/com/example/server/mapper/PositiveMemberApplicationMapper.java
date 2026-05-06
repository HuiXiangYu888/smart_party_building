package com.example.server.mapper;

import com.example.pojo.entity.PositiveMemberApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PositiveMemberApplicationMapper {

	PositiveMemberApplication selectById(@Param("id") Long id);

	List<PositiveMemberApplication> selectByUserId(@Param("userId") Long userId);

	List<PositiveMemberApplication> selectAll();

	int insert(PositiveMemberApplication entity);

	int update(PositiveMemberApplication entity);
}


