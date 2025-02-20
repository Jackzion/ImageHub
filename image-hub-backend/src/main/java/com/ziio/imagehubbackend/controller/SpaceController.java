package com.ziio.imagehubbackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ziio.imagehubbackend.annotation.AuthCheck;
import com.ziio.imagehubbackend.common.BaseResponse;
import com.ziio.imagehubbackend.common.DeleteRequest;
import com.ziio.imagehubbackend.common.ResultUtil;
import com.ziio.imagehubbackend.constant.UserConstant;
import com.ziio.imagehubbackend.entity.Picture;
import com.ziio.imagehubbackend.entity.Space;
import com.ziio.imagehubbackend.entity.SpaceLevel;
import com.ziio.imagehubbackend.entity.User;
import com.ziio.imagehubbackend.enums.SpaceLevelEnum;
import com.ziio.imagehubbackend.exception.BusinessException;
import com.ziio.imagehubbackend.exception.ErrorCode;
import com.ziio.imagehubbackend.exception.ThrowUtils;
import com.ziio.imagehubbackend.request.space.SpaceAddRequest;
import com.ziio.imagehubbackend.request.space.SpaceQueryRequest;
import com.ziio.imagehubbackend.request.space.SpaceUpdateRequest;
import com.ziio.imagehubbackend.request.user.UserQueryRequest;
import com.ziio.imagehubbackend.service.SpaceService;
import com.ziio.imagehubbackend.service.UserService;
import com.ziio.imagehubbackend.vo.SpaceVO;
import com.ziio.imagehubbackend.vo.UserVO;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/space/")
@RestController
public class SpaceController {

    @Resource
    private SpaceService spaceService;
    @Resource
    private UserService userService;

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest){
        if (spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Space space = new Space();
        BeanUtil.copyProperties(spaceUpdateRequest,space);
        // 自动填充数据
        spaceService.fillSpaceBySpaceLevel(space);
        // 数据校验
        spaceService.validSpace(space, false);
        // 判断是否存在
        long id = spaceUpdateRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 更新数据库
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtil.success(true);
    }

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest , HttpServletRequest request){
        if (spaceAddRequest == null || StrUtil.isBlank(spaceAddRequest.getSpaceName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long spaceId = spaceService.addSpace(spaceAddRequest, loginUser);
        return ResultUtil.success(spaceId);
    }

    @GetMapping("/get")
    public BaseResponse<SpaceVO> getSpaceVOById(long id){
        ThrowUtils.throwIf(id <= 0 , ErrorCode.PARAMS_ERROR);
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null , ErrorCode.NOT_FOUND_ERROR);
        return ResultUtil.success(SpaceVO.objToVo(space));
    }

    /**
     * 展示所有空间 Enums
     * @return
     */
    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values()) // 获取所有枚举
                .map(spaceLevelEnum -> new SpaceLevel(
                        spaceLevelEnum.getValue(),
                        spaceLevelEnum.getText(),
                        spaceLevelEnum.getMaxCount(),
                        spaceLevelEnum.getMaxSize()))
                .collect(Collectors.toList());
        return ResultUtil.success(spaceLevelList);
    }

    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<SpaceVO>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest){
        ThrowUtils.throwIf(spaceQueryRequest==null , ErrorCode.PARAMS_ERROR);
        long current = spaceQueryRequest.getCurrent();
        long pageSize = spaceQueryRequest.getPageSize();
        Page<Space> spacePage = spaceService.page(new Page<>(current, pageSize), spaceService.getQueryWrapper(spaceQueryRequest));
        Page<SpaceVO> spaceVoPage = new Page<>(current, pageSize, spacePage.getTotal());
        List<SpaceVO> spaceVOS = spacePage.getRecords().stream().map(SpaceVO::objToVo).collect(Collectors.toList());
        spaceVoPage.setRecords(spaceVOS);
        return ResultUtil.success(spaceVoPage);
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest){
        if(deleteRequest==null || deleteRequest.getId() == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        boolean remove = spaceService.removeById(deleteRequest.getId());
        return ResultUtil.success(true);
    }

}
