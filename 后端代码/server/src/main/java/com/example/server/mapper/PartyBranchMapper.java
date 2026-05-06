package com.example.server.mapper;

import com.example.pojo.entity.PartyBranch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PartyBranchMapper {
    PartyBranch selectById(@Param("id") Long id);
    List<PartyBranch> selectAll();
    int update(PartyBranch partyBranch);
}