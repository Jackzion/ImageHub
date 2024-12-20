package com.ziio.imagehubbackend.aop;

import com.ziio.imagehubbackend.annotation.AuthCheck;
import com.ziio.imagehubbackend.entity.User;
import com.ziio.imagehubbackend.enums.UserRoleEnum;
import com.ziio.imagehubbackend.exception.BusinessException;
import com.ziio.imagehubbackend.exception.ErrorCode;
import com.ziio.imagehubbackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint , AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum requiredRole = UserRoleEnum.getEnumByValue(mustRole);
        // no authcheck continue
        if(requiredRole == null) return joinPoint.proceed();
        // check role
        UserRoleEnum userRole = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        if(userRole == null) throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        if(UserRoleEnum.ADMIN.equals(requiredRole) && !UserRoleEnum.ADMIN.equals(userRole)) throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        // continue
        return joinPoint.proceed();
    }

}
