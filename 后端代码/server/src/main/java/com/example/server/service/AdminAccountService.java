package com.example.server.service;

import com.example.pojo.entity.Admin;

import java.util.List;

/**
 * 账号管理服务
 */
public interface AdminAccountService {

    List<Admin> listAllAdmins();

    List<Admin> listAdminsByBranch(Long branchId);

    Admin createAdmin(Admin admin, Long operatorId);

    int updateAdmin(Admin admin, Long operatorId);

    int updateAdminStatus(Long adminId, String status, Long operatorId);

    int deleteAdmin(Long adminId);
}


