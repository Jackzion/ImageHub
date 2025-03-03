package com.ziio.imagehubbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ziio.imagehubbackend.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ziio.imagehubbackend.entity.User;
import com.ziio.imagehubbackend.request.space.SpaceAddRequest;
import com.ziio.imagehubbackend.request.space.SpaceQueryRequest;
import com.ziio.imagehubbackend.request.space.analyze.SpaceSizeAnalyzeRequest;
import com.ziio.imagehubbackend.vo.space.analyze.SpaceSizeAnalyzeResponse;

import java.util.List;

/**
* @author Ziio
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-02-17 11:27:54
*/
public interface SpaceService extends IService<Space> {

    /**
     * 验证空间配置
     * @param space 空间信息
     * @param add true - 添加 ，false - 修改
     */
    void validSpace(Space space , boolean add);

    /**
     * 根据空间级别填充空间信息
     * @param space 空间信息
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 创建空间(一个用户一个空间 ， 锁 + 事务)
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * request to queryWrapper
     * @param spaceQueryRequest
     * @return
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 检验空间权限
     * @param loginUser
     * @param space
     */
    void checkSpaceAuth(User loginUser, Space space);


}
