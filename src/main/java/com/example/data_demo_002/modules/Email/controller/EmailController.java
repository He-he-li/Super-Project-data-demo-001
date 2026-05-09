package com.example.data_demo_002.modules.Email.controller;


import com.example.data_demo_002.common.result.Result;
import com.example.data_demo_002.common.util.VerilfyCode.VerifyCodeUtil;
import com.example.data_demo_002.modules.Email.service.EmailService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@Tag(name = "邮箱验证码", description = "提供邮箱验证码发送和验证接口")
@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;
    private final VerifyCodeUtil verifyCodeUtil;

    /**
     * 发送邮箱验证码
     * @param email 接收邮箱
     * @return 发送结果
     */
    @PostMapping("/send-code")
    public Result<Void> send(@RequestParam String email) throws Exception {
        System.out.println("发送邮箱验证码：" + email);

        if (verifyCodeUtil.isLimited(email)) {
            return Result.error("60 秒内只能发送一次，请稍后再试");
        }
        String code = verifyCodeUtil.generateCode();
        verifyCodeUtil.saveCode(email, code);
        emailService.sendVerifyCodeEmail(email, code);
        // 生产环境不要返回验证码
        return Result.success(null, "验证码已发送到邮箱：" + email);
    }

    /**
     * 验证邮箱验证码
     * @param email 邮箱
     * @param code 验证码
     * @return 验证结果
     */
    @GetMapping("/check-code")
    public Result<Boolean> check(@RequestParam String email, @RequestParam String code) {
        boolean valid = verifyCodeUtil.checkCode(email, code);
        if (valid) {
            return Result.success(true, "验证成功");
        } else {
            return Result.error("验证码错误或已过期");
        }
    }


    /**
     * TODO
     * 测试发送邮件
     * @param email 接收邮箱
     * @return 发送结果
     */
    //2039658949976387600

}