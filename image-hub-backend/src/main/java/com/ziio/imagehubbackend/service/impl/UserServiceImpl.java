package com.ziio.imagehubbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ziio.imagehubbackend.constant.UserConstant;
import com.ziio.imagehubbackend.entity.User;
import com.ziio.imagehubbackend.enums.UserRoleEnum;
import com.ziio.imagehubbackend.exception.BusinessException;
import com.ziio.imagehubbackend.exception.ErrorCode;
import com.ziio.imagehubbackend.request.user.UserQueryRequest;
import com.ziio.imagehubbackend.service.UserService;
import com.ziio.imagehubbackend.mapper.UserMapper;
import com.ziio.imagehubbackend.vo.LoginUserVO;
import com.ziio.imagehubbackend.vo.UserVO;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author Ziio
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2024-12-20 12:06:19
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // check
        if(StrUtil.hasBlank(userAccount,userPassword,checkPassword)) throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        if(userAccount.length()<4) throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号过短");
        if(userPassword.length()<8||checkPassword.length()<8) throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码过短");
        if(!userPassword.equals(checkPassword)) throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码不一致");
        // check for duplication
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        long count = this.baseMapper.selectCount(queryWrapper);
        if(count>0) throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号重复");
        // get EncryptPassword
        String encryptPassword = getEncryptPassword(userPassword);
        // insert
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("noname");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean save = save(user);
        if(!save) throw new BusinessException(ErrorCode.SYSTEM_ERROR,"注册失败，数据库错误");
        return user.getId();
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // check
        if(StrUtil.hasBlank(userAccount,userPassword)) throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        if(userAccount.length()<4) throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号过短");
        if(userPassword.length()<8) throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码过短");
        // get EncryptPassword
        String encryptPassword = getEncryptPassword(userPassword);
        // verify password
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        queryWrapper.eq("userPassword",encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        if(user == null) throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在或密码错误");
        // put in session
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE,user);
        return this.getLoginUserVO(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if(user == null || user.getId()==null) throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        return user;
    }

    public LoginUserVO getLoginUserVO(User user) {
        if(user==null) return null;
        return BeanUtil.copyProperties(user, LoginUserVO.class);
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        User loginUser = this.getLoginUser(request);
        if(loginUser == null) throw new BusinessException(ErrorCode.PARAMS_ERROR,"not login");
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public UserVO getUserVO(User user) {
        if(user==null) return null;
        return BeanUtil.copyProperties(user, UserVO.class);
    }

    @Override
    public List<UserVO> getUserVOs(List<User> users) {
        if(CollUtil.isEmpty(users)) return new ArrayList<>();
        return users.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if(userQueryRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjectUtil.isNotNull(id),"id",id);
        queryWrapper.eq(StrUtil.isNotBlank(userAccount),"userAccount",userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName),"userName",userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile),"userProfile",userProfile);
        queryWrapper.like(StrUtil.isNotBlank(userRole),"userRole",userRole);
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField),sortOrder.equals("ascend"),sortField);
        return queryWrapper;
    }

    public String getEncryptPassword(String password){
        final String SALT = "ziio";
        return DigestUtils.md5DigestAsHex((SALT + password).getBytes());
    }
}




