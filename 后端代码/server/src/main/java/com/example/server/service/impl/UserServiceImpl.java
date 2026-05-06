package com.example.server.service.impl;

import com.example.pojo.entity.Admin;
import com.example.pojo.entity.Member;
import com.example.server.mapper.AdminMapper;
import com.example.server.mapper.MemberMapper;
import com.example.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public Member findMemberByStudentId(String studentId) {
        return memberMapper.selectByStudentId(studentId);
    }

    @Override
    public Member findMemberByIdNumber(String idNumber) {
        return memberMapper.selectByIdNumber(idNumber);
    }

    @Override
    public Admin findAdminByUsername(String username) {
        return adminMapper.selectByUsername(username);
    }

    @Override
    public Member findMemberById(Long id) {
        return memberMapper.selectById(id);
    }

    @Override
    public Admin findAdminById(Long id) {
        return adminMapper.selectById(id);
    }

    @Override
    public Member findMemberByMobile(String mobile) {
        return memberMapper.selectByMobile(mobile);
    }

    @Override
    public Admin findAdminByMobile(String mobile) {
        return adminMapper.selectByMobile(mobile);
    }
}
