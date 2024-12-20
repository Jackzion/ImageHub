package com.ziio.imagehubbackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ziio.imagehubbackend.annotation.AuthCheck;
import com.ziio.imagehubbackend.common.BaseResponse;
import com.ziio.imagehubbackend.common.DeleteRequest;
import com.ziio.imagehubbackend.common.ResultUtil;
import com.ziio.imagehubbackend.constant.UserConstant;
import com.ziio.imagehubbackend.entity.User;
import com.ziio.imagehubbackend.exception.BusinessException;
import com.ziio.imagehubbackend.exception.ErrorCode;
import com.ziio.imagehubbackend.exception.ThrowUtils;
import com.ziio.imagehubbackend.request.user.*;
import com.ziio.imagehubbackend.service.UserService;
import com.ziio.imagehubbackend.vo.LoginUserVO;
import com.ziio.imagehubbackend.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RequestMapping("/user")
@RestController
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        ThrowUtils.throwIf(userRegisterRequest==null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtil.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        ThrowUtils.throwIf(userLoginRequest==null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO result = userService.userLogin(userAccount, userPassword , request);
        return ResultUtil.success(result);
    }

    @GetMapping("/current")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request){
        User loginUser = this.userService.getLoginUser(request);
        return ResultUtil.success(this.userService.getLoginUserVO(loginUser));
    }

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest){
        ThrowUtils.throwIf(ObjectUtil.isNull(userAddRequest),ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        // default password
        final String DEFAULT_PASSWORD = "12345678";
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        boolean save = userService.save(user);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR);
        return ResultUtil.success(user.getId());
    }

    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserByID(long id){
        ThrowUtils.throwIf(id<=0 , ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user==null , ErrorCode.NOT_FOUND_ERROR);
        return ResultUtil.success(user);
    }

    @GetMapping("/get/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserVO> getUserVOByID(long id){
        BaseResponse<User> userByID = getUserByID(id);
        UserVO userVO = userService.getUserVO(userByID.getData());
        return ResultUtil.success(userVO);
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest){
        if(deleteRequest==null || deleteRequest.getId() == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        boolean remove = userService.removeById(deleteRequest.getId());
        return ResultUtil.success(true);
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest updateRequest){
        ThrowUtils.throwIf(updateRequest==null , ErrorCode.PARAMS_ERROR);
        User user = BeanUtil.copyProperties(updateRequest, User.class);
        boolean update = userService.updateById(user);
        ThrowUtils.throwIf(!update , ErrorCode.SYSTEM_ERROR);
        return ResultUtil.success(true);
    }

    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest){
        ThrowUtils.throwIf(userQueryRequest==null , ErrorCode.PARAMS_ERROR);
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, pageSize), userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
        List<UserVO> userVOs = userService.getUserVOs(userPage.getRecords());
        userVOPage.setRecords(userVOs);
        return ResultUtil.success(userVOPage);
    }
}
