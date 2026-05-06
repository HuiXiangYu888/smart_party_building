package com.example.server.mapper;

import com.example.pojo.entity.PartyApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PartyApplicationMapper {

	PartyApplication selectById(@Param("id") Long id);

	List<PartyApplication> selectByUserId(@Param("userId") Long userId);

	List<PartyApplication> selectAll();

	int insert(PartyApplication entity);

	int update(PartyApplication entity);
}


