package com.example.server.mapper;

import com.example.pojo.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * 用户Mapper接口
 */
@Mapper
public interface MemberMapper {

    /**
     * 根据学号查询用户
     *
     * @param studentId 学号
     * @return 用户信息
     */
    Member selectByStudentId(@Param("studentId") String studentId);

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    Member selectById(@Param("id") Long id);

    /**
     * 根据手机号查询用户
     *
     * @param mobile 手机号
     * @return 用户信息
     */
    Member selectByMobile(@Param("mobile") String mobile);

    /**
     * 根据身份证号查询用户
     *
     * @param idNumber 身份证号
     * @return 用户信息
     */
    Member selectByIdNumber(@Param("idNumber") String idNumber);

    /**
     * 新增用户
     *
     * @param member 用户信息
     * @return 影响行数
     */
    int insert(Member member);

    /**
     * 更新用户信息
     *
     * @param member 用户信息
     * @return 影响行数
     */
    int update(Member member);

    /**
     * 根据ID删除用户
     *
     * @param id 用户ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 统计支部成员人数
     */
    int countByBranchId(@Param("branchId") Long branchId);

    /**
     * 查询全部成员（管理端使用）
     */
    List<Member> selectAll();

    /**
     * 分页条件查询成员
     */
    List<Member> selectWithPagination(@Param("offset") int offset, @Param("limit") int limit,
                                       @Param("branchId") Long branchId,
                                       @Param("reviewStatus") String reviewStatus,
                                       @Param("studentId") String studentId);

    /**
     * 条件查询总数
     */
    int selectCountByCondition(@Param("branchId") Long branchId,
                                @Param("reviewStatus") String reviewStatus,
                                @Param("studentId") String studentId);
}
