package com.example.server.controller.admin;

import com.example.common.result.Result;
import com.example.server.service.AdminAccountService;
import com.example.server.mapper.PartyBranchMapper;
import com.example.pojo.entity.PartyBranch;
import com.example.common.utils.PasswordUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/accounts")
public class AccountController {

    @Autowired
    private AdminAccountService adminAccountService;

    @Autowired
    private PartyBranchMapper partyBranchMapper;

    @Autowired
    private PasswordUtil passwordUtil;

    private boolean isSystemAdmin(HttpServletRequest request) {
        Object userType = request.getAttribute("userType");
        return userType != null && "SYSTEM_ADMIN".equals(userType.toString());
    }

    private Long currentUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        return userId == null ? null : (Long) userId;
    }

    private Long currentUserBranchId(HttpServletRequest request) {
        // 简化处理：前端可传branchId查询，本端严格限制BRANCH_ADMIN只能看本支部
        String branchIdStr = request.getParameter("branchId");
        if (branchIdStr == null) return null;
        try { return Long.parseLong(branchIdStr); } catch (Exception ignored) { return null; }
    }

    @GetMapping
    public Result<List<com.example.pojo.entity.Admin>> list(HttpServletRequest request, @RequestParam(required = false) Long branchId) {
        if (isSystemAdmin(request)) {
            List<com.example.pojo.entity.Admin> list = adminAccountService.listAllAdmins();
            // 附加branchName（前端显示）
            list.forEach(a -> {
                if (a.getBranchId() != null) {
                    PartyBranch b = partyBranchMapper.selectById(a.getBranchId());
                    if (b != null) {
                        a.setBranchName(b.getName());
                    }
                }
            });
            return Result.success(list);
        }
        // 支部管理员只能查看本支部成员（此处按管理员列表返回）
        Long targetBranchId = branchId != null ? branchId : currentUserBranchId(request);
        List<com.example.pojo.entity.Admin> list = adminAccountService.listAdminsByBranch(targetBranchId);
        list.forEach(a -> {
            if (a.getBranchId() != null) {
                PartyBranch b = partyBranchMapper.selectById(a.getBranchId());
                if (b != null) {
                    a.setBranchName(b.getName());
                }
            }
        });
        return Result.success(list);
    }

    @PostMapping
    public Result<com.example.pojo.entity.Admin> create(HttpServletRequest request, @RequestBody com.example.pojo.entity.Admin admin) {
        if (!isSystemAdmin(request)) {
            return Result.error(403, "没有权限添加账号");
        }
        // 参数校验与默认值
        if (admin.getUsername() == null || admin.getUsername().trim().isEmpty()) {
            return Result.error(400, "用户名不能为空");
        }
        if (admin.getMobile() == null || admin.getMobile().trim().isEmpty()) {
            return Result.error(400, "手机号不能为空");
        }
        if (admin.getAdminType() == null || admin.getAdminType().trim().isEmpty()) {
            return Result.error(400, "身份类型不能为空");
        }
        if ("BRANCH_ADMIN".equals(admin.getAdminType())) {
            if (admin.getBranchId() == null) {
                return Result.error(400, "请为支部管理员选择所属支部");
            }
        } else {
            // SYSTEM_ADMIN 不需要支部
            admin.setBranchId(null);
        }
        if (admin.getPassword() == null || admin.getPassword().trim().isEmpty()) {
            admin.setPassword(passwordUtil.getDefaultPassword());
        } else {
            // 前端传明文则加密
            admin.setPassword(passwordUtil.encode(admin.getPassword()));
        }
        if (admin.getStatus() == null) admin.setStatus("ACTIVE");
        com.example.pojo.entity.Admin created = adminAccountService.createAdmin(admin, currentUserId(request));
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result<String> update(HttpServletRequest request, @PathVariable Long id, @RequestBody com.example.pojo.entity.Admin admin) {
        if (!isSystemAdmin(request)) {
            return Result.error(403, "没有权限编辑账号");
        }
        admin.setId(id);
        adminAccountService.updateAdmin(admin, currentUserId(request));
        return Result.success("更新成功");
    }

    @PatchMapping("/{id}/status")
    public Result<Map<String, Object>> updateStatus(HttpServletRequest request, @PathVariable Long id, @RequestParam String status) {
        if (!isSystemAdmin(request)) {
            return Result.error(403, "没有权限启用/禁用账号");
        }
        // 禁止超级管理员禁用自己的账号，避免锁死
        Long operatorId = currentUserId(request);
        if (operatorId != null && operatorId.equals(id) && "INACTIVE".equalsIgnoreCase(status)) {
            return Result.error(400, "不能禁用当前登录的超级管理员账号");
        }
        int rows = adminAccountService.updateAdminStatus(id, status, operatorId);
        Map<String, Object> data = new HashMap<>();
        data.put("updated", rows > 0);
        return Result.success(data);
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(HttpServletRequest request, @PathVariable Long id) {
        if (!isSystemAdmin(request)) {
            return Result.error(403, "没有权限删除账号");
        }
        Long operatorId = currentUserId(request);
        if (operatorId != null && operatorId.equals(id)) {
            return Result.error(400, "不能删除当前登录的超级管理员账号");
        }
        int rows = adminAccountService.deleteAdmin(id);
        return rows > 0 ? Result.success("删除成功") : Result.error(404, "账号不存在");
    }

    @GetMapping("/branches")
    public Result<List<PartyBranch>> listBranches(HttpServletRequest request) {
        if (!isSystemAdmin(request)) {
            return Result.error(403, "没有权限获取支部列表");
        }
        List<PartyBranch> list = partyBranchMapper.selectAll();
        return Result.success(list);
    }
}


