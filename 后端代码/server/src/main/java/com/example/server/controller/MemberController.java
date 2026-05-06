package com.example.server.controller;

import com.example.common.result.Result;
import com.example.pojo.entity.Member;
import com.example.server.mapper.MemberMapper;
import com.example.server.mapper.MemberUpdateRequestMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 成员个人信息接口
 */
@RestController
@RequestMapping("/members")
public class MemberController {

    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private MemberUpdateRequestMapper memberUpdateRequestMapper;

    /**
     * 获取当前登录成员的个人信息
     */
    @GetMapping("/profile")
    public Result<java.util.Map<String, Object>> getProfile(HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        Object userTypeAttr = request.getAttribute("userType");
        if (userIdAttr == null) {
            return Result.error(401, "未登录");
        }
        // 仅允许 MEMBER 角色访问自身信息（如需管理员也可访问，可放开）
        Long userId = (Long) userIdAttr;
        Member member = memberMapper.selectById(userId);
        if (member == null) {
            return Result.error(404, "用户不存在");
        }
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("id", member.getId());
        data.put("studentId", member.getStudentId());
        data.put("idNumber", member.getIdNumber());
        data.put("username", member.getUsername());
        data.put("mobile", member.getMobile());
        data.put("branchId", member.getBranchId());
        // 对外返回中文展示值
        data.put("politicalStatus", toExternalPoliticalStatus(member.getPoliticalStatus()));
        data.put("reviewStatus", member.getReviewStatus() == null ? "PENDING" : member.getReviewStatus());
        return Result.success(data);
    }

    /**
     * 更新当前登录成员的个人信息
     */
    @PutMapping("/profile")
    public Result<String> updateProfile(HttpServletRequest request, @RequestBody Member payload) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) {
            return Result.error(401, "未登录");
        }
        Long userId = (Long) userIdAttr;
        Member exist = memberMapper.selectById(userId);
        if (exist == null) {
            return Result.error(404, "用户不存在");
        }
        // 更新允许字段，并将审核状态重置为未审核（PENDING）
        exist.setStudentId(payload.getStudentId());
        exist.setIdNumber(payload.getIdNumber());
        exist.setUsername(payload.getUsername());
        exist.setMobile(payload.getMobile());
        // 入参与前端中文保持兼容：统一转换为内部枚举代码
        exist.setPoliticalStatus(toInternalPoliticalStatus(payload.getPoliticalStatus()));
        exist.setBranchId(payload.getBranchId());
        // 入党时间可为空
        exist.setJoinDate(payload.getJoinDate());
        // 每次提交都重置为未审核
        exist.setReviewStatus("PENDING");
        memberMapper.update(exist);
        return Result.success("更新成功");
    }

    /**
     * 将内部存储枚举代码转换为中文展示
     */
    private String toExternalPoliticalStatus(String code) {
        if (code == null || code.isBlank()) return "群众";
        switch (code) {
            case "NON_PARTY_MEMBER": 
            case "群众": 
            case "其他": 
                return "群众";
            case "POSITIVE_MEMBER": 
            case "共青团员": 
            case "积极分子": 
                return "共青团员";
            case "PREPARE_MEMBER": 
            case "预备党员": 
                return "预备党员";
            case "PARTY_MEMBER": 
            case "正式党员": 
            case "党员": 
                return "正式党员";
            default: return "群众";
        }
    }

    /**
     * 将前端传入（可能为中文）的政治面貌转换为内部枚举代码，以满足数据库 ENUM 约束
     */
    private String toInternalPoliticalStatus(String value) {
        if (value == null) return "群众";
        String v = value.trim();
        if (v.isEmpty()) return "群众";
        
        // 直接返回中文枚举值（与数据库表结构保持一致）
        if ("群众".equals(v) || "其他".equals(v)) return "群众";
        if ("共青团员".equals(v) || "团员".equals(v) || "积极分子".equals(v)) return "共青团员";
        if ("预备党员".equals(v)) return "预备党员";
        if ("党员".equals(v) || "正式党员".equals(v)) return "正式党员";
        
        // 兼容英文枚举值（如果数据库使用英文枚举）
        if ("NON_PARTY_MEMBER".equalsIgnoreCase(v)) return "群众";
        if ("POSITIVE_MEMBER".equalsIgnoreCase(v)) return "共青团员";
        if ("PREPARE_MEMBER".equalsIgnoreCase(v)) return "预备党员";
        if ("PARTY_MEMBER".equalsIgnoreCase(v)) return "正式党员";
        
        return "群众";
    }
}


