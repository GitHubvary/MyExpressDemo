package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.domain.ResponseResult;
import com.example.demo.domain.bean.User;
import com.example.demo.domain.vo.LayuiTableVO;
import com.example.demo.domain.vo.admin.AdminUserInfoVO;
import com.example.demo.domain.vo.user.UserInfoVO;

import java.util.Map;

public interface UserService extends IService<User> {
    /**
     * Description:根据登录输入信息查找用户
     * @param:用户名和密码
     * @return:用户实体类
    */
    User getByInfo(String username,String password);

    /**
     * Description:根据用户名查找用户信息
     * @param:用户名
     * @return:
    */
    User getByName(String username);

    /**
     * Description:注册信息（用户名、密码和身份）
     * @param:用户名、密码和身份
     * @return:页面返回状态码
    */
    ResponseResult registerByName(String username,String password);

    /**
     * Description:根据用户输入的注册信息判断用户是否存在
     * @param:用户名、密码和身份
     * @return:  存在true,不存在false
    */
    boolean checkExistByUsername(String username);

    /**
     * Description:修改密码
    */
    ResponseResult resetPassword(String userId, String oldPassword, String newPassword);

    /**
     * 获取用户信息
     */
    UserInfoVO getUserInfo(String userId);

    /**
     * 管理员仪表盘展示数据
     */
    Map<String, Integer> getAdminDashboardData();
    /**
     * 获取 AdminUserInfoVO 列表
     */
    LayuiTableVO<AdminUserInfoVO> pageAdminUserInfoVO(Page<User> sysUserPage, QueryWrapper<User> wrapper);
    /**
     * 获取 frontName
     */
    String getFrontName(String userId);
    /**
     * 获取 frontName
     */
    String getFrontName(User user);
}
