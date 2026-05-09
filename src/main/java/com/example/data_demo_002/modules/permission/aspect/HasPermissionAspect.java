package com.example.data_demo_002.modules.permission.aspect;


import com.example.data_demo_002.common.exception.BusinessException;
import com.example.data_demo_002.common.util.permissionUtil.HasPermission;
import com.example.data_demo_002.common.util.Jwt.UserContext;
import com.example.data_demo_002.modules.permission.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class HasPermissionAspect {

    private final PermissionService permissionService;

    /**
     * 环绕通知：在方法执行前检查权限
     */
    @Around("@annotation(hasPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, HasPermission hasPermission) throws Throwable {
        // 获取当前登录用户 ID
        Long userId = UserContext.getUserId();

        // 获取注解中声明的权限编码
        String requiredPermission = hasPermission.value();

        log.debug("权限检查：userId={}, requiredPermission={}", userId, requiredPermission);

        // 检查用户是否有该权限
        if (!permissionService.hasPermission(userId, requiredPermission)) {
            log.warn("权限不足：userId={}, requiredPermission={}", userId, requiredPermission);
            throw new BusinessException("没有权限访问该资源，所需权限：" + requiredPermission);
        }

        log.debug("权限验证通过：userId={}", userId);

        // 权限验证通过，执行目标方法
        return joinPoint.proceed();
    }
}
