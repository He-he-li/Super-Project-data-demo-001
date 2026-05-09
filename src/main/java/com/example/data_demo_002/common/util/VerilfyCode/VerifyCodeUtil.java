package com.example.data_demo_002.common.util.VerilfyCode;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class VerifyCodeUtil {

    private final StringRedisTemplate redisTemplate;
    private final Random random = new Random();

    // 验证码 5 分钟过期
    private static final long CODE_EXPIRE = 5L;
    // 发送频率 60 秒限制
    private static final long LIMIT_EXPIRE = 60L;

    /**
     * 生成 6 位数字验证码
     */
    public String generateCode() {
        return String.format("%06d", random.nextInt(1000000));
    }

    /**
     * 保存验证码 + 记录发送时间（防刷）
     */
    public void saveCode(String email, String code) {
        // 存验证码 5分钟
        redisTemplate.opsForValue().set("verify:code:" + email, code, CODE_EXPIRE, TimeUnit.MINUTES);
        // 存发送时间 60秒
        redisTemplate.opsForValue().set("verify:limit:" + email, "1", LIMIT_EXPIRE, TimeUnit.SECONDS);
    }

    /**
     * 校验验证码
     */
    public boolean checkCode(String email, String code) {
        String cacheCode = redisTemplate.opsForValue().get("verify:code:" + email);
        return code.equals(cacheCode);
    }

    /**
     * 判断是否在 60 秒冷却时间内
     */
    public boolean isLimited(String email) {
        return redisTemplate.hasKey("verify:limit:" + email);
    }
}