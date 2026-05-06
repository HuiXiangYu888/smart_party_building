package com.example.server.mapper;

import com.example.pojo.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 管理员Mapper接口
 */
@Mapper
public interface AdminMapper {

    /**
     * 根据用户名查询管理员
     *
     * @param username 用户名
     * @return 管理员信息
     */
    Admin selectByUsername(@Param("username") String username);

    /**
     * 根据ID查询管理员
     *
     * @param id 管理员ID
     * @return 管理员信息
     */
    Admin selectById(@Param("id") Long id);

    /**
     * 根据手机号查询管理员
     *
     * @param mobile 手机号
     * @return 管理员信息
     */
    Admin selectByMobile(@Param("mobile") String mobile);

    /**
     * 根据支部ID查询管理员列表
     *
     * @param branchId 支部ID
     * @return 管理员列表
     */
    List<Admin> selectByBranchId(@Param("branchId") Long branchId);

    /**
     * 根据管理员类型查询管理员列表
     *
     * @param adminType 管理员类型
     * @return 管理员列表
     */
    List<Admin> selectByAdminType(@Param("adminType") String adminType);

    /**
     * 新增管理员
     *
     * @param admin 管理员信息
     * @return 影响行数
     */
    int insert(Admin admin);

    /**
     * 更新管理员信息
     *
     * @param admin 管理员信息
     * @return 影响行数
     */
    int update(Admin admin);

    /**
     * 根据ID删除管理员
     *
     * @param id 管理员ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 更新管理员密码
     *
     * @param id 管理员ID
     * @param password 新密码
     * @return 影响行数
     */
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    /**
     * 根据ID列表批量查询管理员
     */
    List<Admin> selectByIds(@Param("ids") List<Long> ids);
}
