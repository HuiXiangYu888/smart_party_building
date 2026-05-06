package com.example.server.service;

import com.example.pojo.entity.Admin;
import com.example.pojo.entity.Member;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 根据学号查找用户
     *
     * @param studentId 学号
     * @return 用户信息
     */
    Member findMemberByStudentId(String studentId);

    /**
     * 根据身份证号查找用户
     *
     * @param idNumber 身份证号
     * @return 用户信息
     */
    Member findMemberByIdNumber(String idNumber);

    /**
     * 根据用户名查找管理员
     *
     * @param username 用户名
     * @return 管理员信息
     */
    Admin findAdminByUsername(String username);

    /**
     * 根据ID查找用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    Member findMemberById(Long id);

    /**
     * 根据ID查找管理员
     *
     * @param id 管理员ID
     * @return 管理员信息
     */
    Admin findAdminById(Long id);

    /**
     * 根据手机号查找用户
     *
     * @param mobile 手机号
     * @return 用户信息
     */
    Member findMemberByMobile(String mobile);

    /**
     * 根据手机号查找管理员
     *
     * @param mobile 手机号
     * @return 管理员信息
     */
    Admin findAdminByMobile(String mobile);
}
