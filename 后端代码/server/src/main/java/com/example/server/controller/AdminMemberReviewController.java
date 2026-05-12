package com.example.server.controller;

import com.example.common.result.Result;
import com.example.pojo.entity.Member;
import com.example.server.mapper.MemberMapper;
import com.example.server.mapper.AdminMapper;
import com.example.pojo.entity.Admin;
import com.example.server.mapper.PartyBranchMapper;
import com.example.pojo.entity.PartyBranch;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/members")
public class AdminMemberReviewController {

    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private PartyBranchMapper partyBranchMapper;

    /**
     * 分页查询成员列表（真分页）
     */
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(value = "reviewStatus", required = false) String reviewStatus,
            @RequestParam(value = "studentId", required = false) String studentId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            HttpServletRequest request) {

        Object userType = request.getAttribute("userType");
        if (userType == null) return Result.error(401, "未登录");
        String type = userType.toString();
        if (!"SYSTEM_ADMIN".equals(type) && !"BRANCH_ADMIN".equals(type)) {
            return Result.error(403, "无权限");
        }

        // Determine branch scope for BRANCH_ADMIN
        Long branchId = null;
        if ("BRANCH_ADMIN".equals(type)) {
            Long adminId = (Long) request.getAttribute("userId");
            Admin admin = adminMapper.selectById(adminId);
            if (admin != null && admin.getBranchId() != null) {
                branchId = admin.getBranchId();
            } else {
                // No branch bound, return empty
                Map<String, Object> empty = new HashMap<>();
                empty.put("records", Collections.emptyList());
                empty.put("total", 0);
                return Result.success(empty);
            }
        }

        // Normalize empty strings to null
        if (reviewStatus != null && reviewStatus.isEmpty()) reviewStatus = null;
        if (studentId != null && studentId.isEmpty()) studentId = null;

        int offset = (page - 1) * size;
        List<Member> list = memberMapper.selectWithPagination(offset, size, branchId, reviewStatus, studentId);
        int total = memberMapper.selectCountByCondition(branchId, reviewStatus, studentId);

        // Build response with branch names and status labels
        List<Map<String, Object>> records = list.stream().map(m -> {
            Map<String, Object> x = new HashMap<>();
            x.put("id", m.getId());
            x.put("name", m.getUsername());
            x.put("studentId", m.getStudentId());
            x.put("idNumber", m.getIdNumber());
            String mobile = m.getMobile() == null ? "" : m.getMobile();
            x.put("mobile", mobile);
            x.put("phone", mobile);
            x.put("politicalStatus", m.getPoliticalStatus());

            // Branch name
            String branchName = "-";
            if (m.getBranchId() != null) {
                PartyBranch b = partyBranchMapper.selectById(m.getBranchId());
                if (b != null) branchName = b.getName();
            }
            x.put("branchName", branchName);

            // Status label
            String st = m.getReviewStatus();
            String stZh = "待审核";
            if ("APPROVED".equals(st)) stZh = "已通过";
            else if ("REJECTED".equals(st)) stZh = "不通过";
            x.put("reviewStatus", stZh);
            return x;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", total);
        return Result.success(data);
    }

    @PostMapping("/review/{id}")
    public Result<String> review(@PathVariable Long id, @RequestParam("approve") boolean approve, HttpServletRequest request) {
        Object userType = request.getAttribute("userType");
        if (userType == null) return Result.error(401, "未登录");
        String type = userType.toString();
        if (!"SYSTEM_ADMIN".equals(type) && !"BRANCH_ADMIN".equals(type)) {
            return Result.error(403, "无权限");
        }
        Member m = memberMapper.selectById(id);
        if (m == null) return Result.error(404, "用户不存在");

        // Branch admin can only review members in their branch
        if ("BRANCH_ADMIN".equals(type)) {
            Long adminId = (Long) request.getAttribute("userId");
            Admin admin = adminMapper.selectById(adminId);
            if (admin == null || admin.getBranchId() == null || !admin.getBranchId().equals(m.getBranchId())) {
                return Result.error(403, "只能审核所属支部的成员");
            }
        }

        m.setReviewStatus(approve ? "APPROVED" : "REJECTED");
        memberMapper.update(m);
        return Result.success("审核完成");
    }

    @PostMapping("/review/batch")
    public Result<String> batch(@RequestParam("approve") boolean approve, HttpServletRequest request) {
        Object userType = request.getAttribute("userType");
        if (userType == null) return Result.error(401, "未登录");
        String type = userType.toString();
        if (!"SYSTEM_ADMIN".equals(type) && !"BRANCH_ADMIN".equals(type)) {
            return Result.error(403, "无权限");
        }

        List<Member> list = memberMapper.selectAll();

        if ("BRANCH_ADMIN".equals(type)) {
            Long adminId = (Long) request.getAttribute("userId");
            Admin admin = adminMapper.selectById(adminId);
            if (admin != null && admin.getBranchId() != null) {
                Long bid = admin.getBranchId();
                list = list.stream().filter(m -> bid.equals(m.getBranchId())).collect(Collectors.toList());
            } else {
                list = Collections.emptyList();
            }
        }

        int cnt = 0;
        for (Member m : list) {
            if (!"PENDING".equals(m.getReviewStatus())) continue;
            m.setReviewStatus(approve ? "APPROVED" : "REJECTED");
            memberMapper.update(m);
            cnt++;
        }
        return Result.success("处理完成：" + cnt + "条");
    }
}
