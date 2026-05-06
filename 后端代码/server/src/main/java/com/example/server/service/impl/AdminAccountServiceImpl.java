package com.example.server.service.impl;

import com.example.server.mapper.AdminMapper;
import com.example.server.service.AdminAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;

@Service
public class AdminAccountServiceImpl implements AdminAccountService {

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public List<com.example.pojo.entity.Admin> listAllAdmins() {
        // 复用按类型查询，拉取两类管理员
        // 此处简单返回 SYSTEM_ADMIN + BRANCH_ADMIN 的并集，也可编写新的SQL
        List<com.example.pojo.entity.Admin> system = adminMapper.selectByAdminType("SYSTEM_ADMIN");
        List<com.example.pojo.entity.Admin> branch = adminMapper.selectByAdminType("BRANCH_ADMIN");
        List<com.example.pojo.entity.Admin> all = new ArrayList<>();
        if (system != null) all.addAll(system);
        if (branch != null) all.addAll(branch);
        return all;
    }

    @Override
    public List<com.example.pojo.entity.Admin> listAdminsByBranch(Long branchId) {
        return adminMapper.selectByBranchId(branchId);
    }

    @Override
    public com.example.pojo.entity.Admin createAdmin(com.example.pojo.entity.Admin admin, Long operatorId) {
        admin.setLastModifiedBy(operatorId);
        if (admin.getStatus() == null) {
            admin.setStatus("ACTIVE");
        }
        adminMapper.insert(admin);
        return admin;
    }

    @Override
    public int updateAdmin(com.example.pojo.entity.Admin admin, Long operatorId) {
        admin.setLastModifiedBy(operatorId);
        return adminMapper.update(admin);
    }

    @Override
    public int updateAdminStatus(Long adminId, String status, Long operatorId) {
        com.example.pojo.entity.Admin toUpdate = adminMapper.selectById(adminId);
        if (toUpdate == null) {
            return 0;
        }
        toUpdate.setStatus(status);
        toUpdate.setLastModifiedBy(operatorId);
        return adminMapper.update(toUpdate);
    }

    @Override
    public int deleteAdmin(Long adminId) {
        return adminMapper.deleteById(adminId);
    }
}


