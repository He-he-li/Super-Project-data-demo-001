package com.example.data_demo_002.modules.user.dao;


import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class BatchAssignRolesRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "用户ID列表不能为空")
    private List<Long> userIds;

    @NotEmpty(message = "角色ID列表不能为空")
    private List<Long> roleIds;
}
