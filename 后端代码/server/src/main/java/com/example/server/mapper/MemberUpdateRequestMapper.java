package com.example.server.mapper;

import com.example.pojo.entity.MemberUpdateRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MemberUpdateRequestMapper {
    int insert(MemberUpdateRequest req);
    int update(MemberUpdateRequest req);
    MemberUpdateRequest selectPendingByMemberId(@Param("memberId") Long memberId);
    MemberUpdateRequest selectById(@Param("id") Long id);
    List<MemberUpdateRequest> selectAll(@Param("status") String status);
}


