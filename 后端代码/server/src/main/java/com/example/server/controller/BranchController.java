package com.example.server.controller;

import com.example.common.result.Result;
import com.example.pojo.entity.Admin;
import com.example.pojo.entity.PartyBranch;
import com.example.server.mapper.AdminMapper;
import com.example.server.mapper.MemberMapper;
import com.example.server.mapper.PartyBranchMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/branches")
public class BranchController {

    @Autowired
    private PartyBranchMapper partyBranchMapper;
    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private AdminMapper adminMapper;

    /**
     * 获取支部信息（含人数、负责人姓名与电话）
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getBranch(@PathVariable Long id) {
        PartyBranch b = partyBranchMapper.selectById(id);
        if (b == null) return Result.error(404, "支部不存在");
        int count = memberMapper.countByBranchId(id);
        Map<String, Object> data = new HashMap<>();
        data.put("id", b.getId());
        data.put("name", b.getName());
        data.put("memberCount", count);
        data.put("leaderId", b.getLeaderId());
        Admin leader = b.getLeaderId() == null ? null : adminMapper.selectById(b.getLeaderId());
        data.put("leaderName", leader != null ? leader.getUsername() : null);
        data.put("leaderMobile", leader != null ? leader.getMobile() : null);
        return Result.success(data);
    }

    /**
     * 获取所有支部列表（用户端使用）
     */
    @GetMapping
    public Result<List<PartyBranch>> getAllBranches() {
        List<PartyBranch> branches = partyBranchMapper.selectAll();
        return Result.success(branches);
    }

    /**
     * 更新支部信息（名称、负责人）
     */
    @PutMapping("/{id}")
    public Result<String> updateBranch(HttpServletRequest request, @PathVariable Long id, @RequestBody PartyBranch payload) {
        // 权限：系统管理员可更新任何支部；支部管理员仅可更新自己支部
        Object userType = request.getAttribute("userType");
        Object userIdAttr = request.getAttribute("userId");
        if (userType == null || userIdAttr == null) return Result.error(401, "未登录");
        PartyBranch exist = partyBranchMapper.selectById(id);
        if (exist == null) return Result.error(404, "支部不存在");
        String type = userType.toString();
        Long uid = (Long) userIdAttr;
        if (!"SYSTEM_ADMIN".equals(type)) {
            // 非系统管理员需验证是否为该支部负责人或该支部管理员
            Admin admin = adminMapper.selectById(uid);
            if (admin == null) return Result.error(403, "无权限");
            if (admin.getBranchId() == null || !admin.getBranchId().equals(id)) {
                return Result.error(403, "无权限更新该支部");
            }
        }
        exist.setName(payload.getName());
        exist.setLeaderId(payload.getLeaderId());
        partyBranchMapper.update(exist);
        return Result.success("更新成功");
    }
}


