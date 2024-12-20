package com.ziio.imagehubbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ziio.imagehubbackend.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ziio.imagehubbackend.request.user.UserQueryRequest;
import com.ziio.imagehubbackend.vo.LoginUserVO;
import com.ziio.imagehubbackend.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author Ziio
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2024-12-20 12:06:19
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount , String userPassword , String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 转换 VO
     * @param user
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 转换 userVO
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 转换 userVO list
     * @param users
     * @return
     */
    List<UserVO> getUserVOs(List<User> users);

    /**
     * 转换 queryWrapper
     * @param request
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest request);

    /**
     * 获得加密密码
     * @param password
     * @return
     */
    String getEncryptPassword(String password);
}
