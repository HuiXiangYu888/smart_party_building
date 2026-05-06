package com.example.server.controller;

import com.example.common.result.Result;
import com.example.pojo.entity.Member;
import com.example.pojo.entity.MemberUpdateRequest;
import com.example.pojo.entity.SystemSetting;
import com.example.server.mapper.MemberMapper;
import com.example.server.mapper.MemberUpdateRequestMapper;
import com.example.server.mapper.SystemSettingMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/members")
public class MemberProfileController {

    private static final String KEY_OPEN_APPLICATION = "OPEN_APPLICATION";

    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private MemberUpdateRequestMapper memberUpdateRequestMapper;
    @Autowired
    private SystemSettingMapper systemSettingMapper;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // moved profile GET to MemberController to avoid duplicate mappings

    @PostMapping("/profile/submit")
    public Result<String> submitProfileUpdate(HttpServletRequest request, @RequestBody Map<String, Object> payload) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) return Result.error(401, "未登录");
        Long userId = (Long) userIdAttr;

        SystemSetting s = systemSettingMapper.selectByKey(KEY_OPEN_APPLICATION);
        boolean open = s != null && "true".equalsIgnoreCase(s.getSettingValue());

        if (!open) {
            return Result.error(403, "当前未开放个人信息申请");
        }

        MemberUpdateRequest exist = memberUpdateRequestMapper.selectPendingByMemberId(userId);
        String fieldsJson;
        try {
            fieldsJson = OBJECT_MAPPER.writeValueAsString(payload);
        } catch (Exception e) {
            return Result.error(400, "提交数据格式错误");
        }
        if (exist != null) {
            // 覆盖更新已有待审记录
            exist.setFieldsJson(fieldsJson);
            exist.setStatus("PENDING");
            memberUpdateRequestMapper.update(exist);
            return Result.success("已更新待审核申请");
        }

        MemberUpdateRequest req = new MemberUpdateRequest();
        req.setMemberId(userId);
        req.setFieldsJson(fieldsJson);
        req.setStatus("PENDING");
        req.setCreatedAt(LocalDateTime.now());
        memberUpdateRequestMapper.insert(req);
        return Result.success("已提交审核");
    }

    @PostMapping("/profile/review/{id}")
    public Result<String> review(@PathVariable Long id, @RequestParam("approve") boolean approve, @RequestParam(value = "remark", required = false) String remark, HttpServletRequest request) {
        Object userType = request.getAttribute("userType");
        if (userType == null) return Result.error(401, "未登录");
        String type = userType.toString();
        if (!"SYSTEM_ADMIN".equals(type) && !"BRANCH_ADMIN".equals(type)) {
            return Result.error(403, "无审核权限");
        }

        MemberUpdateRequest req = memberUpdateRequestMapper.selectById(id);
        if (req == null) return Result.error(404, "申请不存在");
        if (!"PENDING".equals(req.getStatus())) return Result.error(400, "申请已处理");

        if (approve) {
            // 应用变更
            Map<String, Object> fields;
            try {
                fields = OBJECT_MAPPER.readValue(req.getFieldsJson(), new TypeReference<Map<String, Object>>(){});
            } catch (Exception e) {
                return Result.error(400, "待审核数据解析失败");
            }
            Member m = memberMapper.selectById(req.getMemberId());
            if (m == null) return Result.error(404, "用户不存在");
            if (fields.containsKey("studentId")) m.setStudentId((String) fields.get("studentId"));
            if (fields.containsKey("idNumber")) m.setIdNumber((String) fields.get("idNumber"));
            if (fields.containsKey("username")) m.setUsername((String) fields.get("username"));
            if (fields.containsKey("mobile")) m.setMobile((String) fields.get("mobile"));
            if (fields.containsKey("branchId")) {
                Object v = fields.get("branchId");
                if (v instanceof Number) m.setBranchId(((Number) v).longValue());
                else if (v instanceof String) try { m.setBranchId(Long.parseLong((String) v)); } catch (Exception ignore) {}
            }
            memberMapper.update(m);
            req.setStatus("APPROVED");
        } else {
            req.setStatus("REJECTED");
        }
        req.setReviewerId((Long) request.getAttribute("userId"));
        req.setRemark(remark);
        req.setReviewedAt(LocalDateTime.now());
        memberUpdateRequestMapper.update(req);
        return Result.success("审核完成");
    }

    @GetMapping("/profile/requests")
    public Result<List<Map<String, Object>>> listRequests(@RequestParam(value = "status", required = false) String status, HttpServletRequest request) {
        Object userType = request.getAttribute("userType");
        if (userType == null) return Result.error(401, "未登录");
        String type = userType.toString();
        if (!"SYSTEM_ADMIN".equals(type) && !"BRANCH_ADMIN".equals(type)) {
            return Result.error(403, "无权限");
        }
        List<MemberUpdateRequest> list = memberUpdateRequestMapper.selectAll(status);
        List<Map<String, Object>> resp = new java.util.ArrayList<>();
        for (MemberUpdateRequest r : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            Member mb = memberMapper.selectById(r.getMemberId());
            m.put("applicantName", mb == null ? ("#" + r.getMemberId()) : mb.getUsername());
            m.put("type", "资料变更");
            m.put("submitTime", r.getCreatedAt());
            String st = r.getStatus();
            if ("PENDING".equals(st)) m.put("status", "待审核");
            else if ("APPROVED".equals(st)) m.put("status", "已通过");
            else if ("REJECTED".equals(st)) m.put("status", "已拒绝");
            else m.put("status", st);
            m.put("reviewer", r.getReviewerId());
            m.put("reviewTime", r.getReviewedAt());
            m.put("remark", r.getRemark());
            resp.add(m);
        }
        return Result.success(resp);
    }

    @PostMapping("/profile/review/batch")
    public Result<String> batchReview(@RequestParam("approve") boolean approve, HttpServletRequest request) {
        Object userType = request.getAttribute("userType");
        if (userType == null) return Result.error(401, "未登录");
        String type = userType.toString();
        if (!"SYSTEM_ADMIN".equals(type) && !"BRANCH_ADMIN".equals(type)) {
            return Result.error(403, "无审核权限");
        }
        List<MemberUpdateRequest> list = memberUpdateRequestMapper.selectAll("PENDING");
        int processed = 0;
        for (MemberUpdateRequest req : list) {
            if (!"PENDING".equals(req.getStatus())) continue;
            if (approve) {
                Map<String, Object> fields;
                try {
                    fields = OBJECT_MAPPER.readValue(req.getFieldsJson(), new TypeReference<Map<String, Object>>(){});
                } catch (Exception e) {
                    continue;
                }
                Member m = memberMapper.selectById(req.getMemberId());
                if (m == null) continue;
                if (fields.containsKey("studentId")) m.setStudentId((String) fields.get("studentId"));
                if (fields.containsKey("idNumber")) m.setIdNumber((String) fields.get("idNumber"));
                if (fields.containsKey("username")) m.setUsername((String) fields.get("username"));
                if (fields.containsKey("mobile")) m.setMobile((String) fields.get("mobile"));
                if (fields.containsKey("branchId")) {
                    Object v = fields.get("branchId");
                    if (v instanceof Number) m.setBranchId(((Number) v).longValue());
                    else if (v instanceof String) try { m.setBranchId(Long.parseLong((String) v)); } catch (Exception ignore) {}
                }
                memberMapper.update(m);
                req.setStatus("APPROVED");
            } else {
                req.setStatus("REJECTED");
            }
            req.setReviewerId((Long) request.getAttribute("userId"));
            req.setReviewedAt(LocalDateTime.now());
            memberUpdateRequestMapper.update(req);
            processed++;
        }
        return Result.success("处理完成：" + processed + "条");
    }
}


