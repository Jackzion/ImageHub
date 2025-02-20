package com.ziio.imagehubbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ziio.imagehubbackend.entity.Space;
import com.ziio.imagehubbackend.entity.User;
import com.ziio.imagehubbackend.enums.SpaceLevelEnum;
import com.ziio.imagehubbackend.exception.BusinessException;
import com.ziio.imagehubbackend.exception.ErrorCode;
import com.ziio.imagehubbackend.exception.ThrowUtils;
import com.ziio.imagehubbackend.request.space.SpaceAddRequest;
import com.ziio.imagehubbackend.request.space.SpaceQueryRequest;
import com.ziio.imagehubbackend.request.user.UserQueryRequest;
import com.ziio.imagehubbackend.service.SpaceService;
import com.ziio.imagehubbackend.mapper.SpaceMapper;
import com.ziio.imagehubbackend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
* @author Ziio
* @description 针对表【space(空间)】的数据库操作Service实现
* @createDate 2025-02-17 11:27:54
*/
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
    implements SpaceService{

    @Resource
    UserService userService;

    @Resource
    TransactionTemplate transactionTemplate;

    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        // 添加数据时
        if(add){
            if (StrUtil.isBlank(spaceName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            }
            if (spaceLevel == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不能为空");
            }
        }
        // 修改数据时
        if(spaceLevel != null && spaceLevelEnum == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别错误");
        }
        if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
        }
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if(spaceLevelEnum!=null){
            long maxSize = spaceLevelEnum.getMaxSize();
            if (space.getMaxSize() == null) {
                space.setMaxSize(maxSize);
            }
            long maxCount = spaceLevelEnum.getMaxCount();
            if (space.getMaxCount() == null) {
                space.setMaxCount(maxCount);
            }
        }
    }

    Map<Long,Object> lockMap = new ConcurrentHashMap<>();

    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        Space space = new Space();
        BeanUtil.copyProperties(spaceAddRequest, space);
        // 默认值
        if (StrUtil.isBlank(spaceAddRequest.getSpaceName())) {
            space.setSpaceName("默认空间");
        }
        if (spaceAddRequest.getSpaceLevel() == null) {
            spaceAddRequest.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        // 填充数据
        this.fillSpaceBySpaceLevel(space);
        // 数据校验
        this.validSpace(space, true);
        Long userId = loginUser.getId();
        space.setUserId(userId);
        // 权限校验
        if (SpaceLevelEnum.COMMON.getValue() != spaceAddRequest.getSpaceLevel() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限创建指定级别的空间");
        }
        // 针对用户加锁
        // 本地锁优化
        Object lock = lockMap.computeIfAbsent(userId, k -> new Object());
        synchronized (lock){
            try{
                Long newSpaceId = transactionTemplate.execute(status -> {
                    boolean exists = this.lambdaQuery().eq(Space::getUserId, userId).exists();
                    ThrowUtils.throwIf(exists,ErrorCode.OPERATION_ERROR, "用户已存在空间");
                    // 写入数据库
                    boolean result = this.save(space);
                    ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR, "创建空间失败");
                    // 返回写入的数据 id
                    return space.getId();
                });
                return Optional.ofNullable(newSpaceId).orElse(-1L);
            }finally {
                // 防止内存泄露
                lockMap.remove(userId);
            }
        }
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        if(spaceQueryRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        String spaceName = spaceQueryRequest.getSpaceName();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();

        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjectUtil.isNotNull(id),"id",id);
        queryWrapper.eq(ObjectUtil.isNotNull(userId),"userId",userId);
        queryWrapper.eq(ObjectUtil.isNotNull(spaceLevel),"spaceLevel",spaceLevel);
        queryWrapper.like(StrUtil.isNotBlank(spaceName),"spaceName",spaceName);
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField),sortOrder.equals("ascend"),sortField);
        return queryWrapper;
    }
}




