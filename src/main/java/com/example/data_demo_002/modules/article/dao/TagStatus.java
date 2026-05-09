package com.example.data_demo_002.modules.article.dao;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum TagStatus {
    ENABLED(0, "启用"),
    DISABLED(1, "禁用");

    private final Integer value;
    private final String description;

    TagStatus(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    @JsonValue
    public Integer getValue() {
        return value;
    }

    @JsonCreator
    public static TagStatus fromValue(Integer value) {
        for (TagStatus status : TagStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的状态值：" + value);
    }
}
