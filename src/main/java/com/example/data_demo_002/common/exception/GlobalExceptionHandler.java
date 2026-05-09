package com.example.data_demo_002.common.exception;



import com.example.data_demo_002.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice // 标记为全局异常处理器，只针对 @RestController
public class GlobalExceptionHandler {

    /**
     * 1. 捕获自定义业务异常 (BusinessException)
     * 场景：主动抛出的逻辑错误，如 "用户不存在"
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        // 记录警告日志，不算系统错误
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 2. 捕获参数校验异常 (@Valid / @Validated)
     * 场景：前端传参不符合 @NotBlank, @Size 等规则
     * 注意：Spring Boot 2.3+ 抛出 MethodArgumentNotValidException (JSON body)
     *       或 BindException (表单/路径参数)
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleValidationExceptions(Exception e) {
        String message;

        if (e instanceof MethodArgumentNotValidException ex) {
            // 提取第一个错误字段的提示信息
            message = ex.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
        } else if (e instanceof BindException ex) {
            message = ex.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
        } else {
            message = "参数校验失败";
        }

        log.warn("参数校验异常: {}", message);
        // 返回 400 或自定义码，这里用 400 表示客户端错误
        return Result.error(400, message);
    }

    /**
     * 3. 捕获 404 资源未找到
     * 场景：访问了不存在的 URL
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> handleNotFound(NoResourceFoundException e) {
        return Result.error(404, "接口路径不存在：" + e.getResourcePath());
    }

    /**
     * 4. 捕获所有其他未知异常 (兜底)
     * 场景：代码空指针、数据库连接断开、系统崩溃等
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleGlobalException(Exception e) {
        // ⭐️ 重要：必须打印堆栈日志，方便排查问题
        log.error("系统内部异常:", e);

        // 生产环境建议不要直接把 e.getMessage() 返回给前端，防止泄露敏感信息
        // 这里简单返回 "系统繁忙，请稍后再试"
        return Result.error(500, "系统内部错误，请联系管理员");
    }
}