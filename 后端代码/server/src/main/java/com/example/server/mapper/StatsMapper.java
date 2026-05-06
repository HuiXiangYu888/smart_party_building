package com.example.server.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface StatsMapper {
    int countMembers();
    int countPendingApplications();
    int countOngoingActivities();
    int countPendingAssignments();
    List<Map<String, Object>> selectBranchStudyDurations();
    
    // 按支部ID统计的方法
    int countMembersByBranchId(Long branchId);
    int countPendingApplicationsByBranchId(Long branchId);
    int countOngoingActivitiesByBranchId(Long branchId);
    int countPendingAssignmentsByBranchId(Long branchId);
}


