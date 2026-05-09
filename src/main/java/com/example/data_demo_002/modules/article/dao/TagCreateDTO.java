package com.example.data_demo_002.modules.article.dao;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 标签创建请求 DTO
 */
@Data
public class TagCreateDTO {

    /**
     * 标签名称
     */
    @NotBlank(message = "标签名称不能为空")
    private String tagName;

    /**
     * 状态：0-启用，1-禁用（默认 0）
     */
    private Integer status = 0;
}
