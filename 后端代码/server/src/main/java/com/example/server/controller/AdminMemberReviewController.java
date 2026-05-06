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

import java.util.List;
import java.util.Map;
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

    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestParam(value = "reviewStatus", required = false) String reviewStatus,
                                                  @RequestParam(value = "studentId", required = false) String studentId,
                                                  HttpServletRequest request) {
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
                Long branchId = admin.getBranchId();
                list = list.stream().filter(m -> branchId.equals(m.getBranchId())).collect(Collectors.toList());
            } else {
                list = java.util.Collections.emptyList();
            }
        }
        if (reviewStatus != null && !reviewStatus.isEmpty()) {
            list = list.stream().filter(m -> reviewStatus.equals(m.getReviewStatus())).collect(Collectors.toList());
        }
        if (studentId != null && !studentId.isEmpty()) {
            String sid = studentId.trim();
            list = list.stream().filter(m -> m.getStudentId() != null && m.getStudentId().contains(sid)).collect(Collectors.toList());
        }
        List<Map<String, Object>> resp = list.stream().map(m -> {
            java.util.Map<String, Object> x = new java.util.HashMap<>();
            x.put("id", m.getId());
            x.put("name", m.getUsername());
            x.put("studentId", m.getStudentId());
            x.put("idNumber", m.getIdNumber());
            String mobile = m.getMobile() == null ? "" : m.getMobile();
            x.put("mobile", mobile);
            // 兼容前端可能使用的字段名
            x.put("phone", mobile);
            // 政治面貌：直接返回数据库原值
            x.put("politicalStatus", m.getPoliticalStatus());
            // 支部名称
            String branchName = "-";
            if (m.getBranchId() != null) {
                PartyBranch b = partyBranchMapper.selectById(m.getBranchId());
                if (b != null) branchName = b.getName();
            }
            x.put("branchName", branchName);
            // 审核状态中文
            String st = m.getReviewStatus();
            String stZh = "待审核";
            if ("APPROVED".equals(st)) stZh = "已通过";
            else if ("REJECTED".equals(st)) stZh = "不通过";
            x.put("reviewStatus", stZh);
            return x;
        }).collect(Collectors.toList());
        return Result.success(resp);
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
        
        // 支部管理员只能审核所属支部的成员
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
        
        // 支部管理员只能批量处理所属支部的成员
        if ("BRANCH_ADMIN".equals(type)) {
            Long adminId = (Long) request.getAttribute("userId");
            Admin admin = adminMapper.selectById(adminId);
            if (admin != null && admin.getBranchId() != null) {
                Long branchId = admin.getBranchId();
                list = list.stream().filter(m -> branchId.equals(m.getBranchId())).collect(Collectors.toList());
            } else {
                list = java.util.Collections.emptyList();
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


