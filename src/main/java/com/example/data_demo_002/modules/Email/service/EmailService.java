package com.example.data_demo_002.modules.Email.service;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;

@Service
@RequiredArgsConstructor
public class EmailService {

    // 邮件发送者
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine; // 模板引擎

    @Value("${spring.mail.username}")
    private String fromEmail;



    /**
     *  发送验证码邮件
     * @param to 接收邮箱
     * @param code 验证码
     * @throws MessagingException 邮件发送异常
     */
    public void sendVerifyCodeEmail(String to, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject("【账号验证】您的验证码");

        // 使用 Thymeleaf 模板渲染验证码
        Context context = new Context();
        context.setVariable("code", code);
        String html = templateEngine.process("email-code", context);

        helper.setText(html, true);
        mailSender.send(message);
    }


    /**
     * 发送简单邮件
     * @param to 接收邮箱
     * @param subject 邮件标题
     * @param text 邮件内容
     */
    public void sendSimpleMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    /**
     * 发送 HTML 邮件
     * @param to 接收邮箱
     * @param subject 邮件标题
     * @param htmlContent HTML 内容
     * @throws MessagingException 邮件发送异常
     */
    public void sendHtmlMail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }



    /**
     *  发送带附件的邮件
     * @param to 接收邮箱
     * @param subject 邮件标题
     * @param text 邮件内容
     * @param filePath 附件路径
     * @throws MessagingException 邮件发送异常
     */
    public void sendAttachmentMail(String to, String subject, String text, String filePath) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text);

        FileSystemResource file = new FileSystemResource(new File(filePath));
        helper.addAttachment("附件", file);
        mailSender.send(message);
    }
}