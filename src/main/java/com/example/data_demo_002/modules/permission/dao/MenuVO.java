package com.example.data_demo_002.modules.permission.dao;


import lombok.Data;
import java.util.List;

/**
 * 菜单树形结构 VO
 */
@Data
public class MenuVO {
    private Long id;
    private Long parentId;
    private String name;
    private String path;
    private String component;
    private String icon;
    private Integer menuType;
    private List<MenuVO> children;
}
