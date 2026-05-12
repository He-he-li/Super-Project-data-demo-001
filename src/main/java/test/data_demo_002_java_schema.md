# 📊 data_demo_002_java 数据库结构文档

- **Schema**: public
- **生成时间**: 2026-05-12 17:46:10

---

## 📋 表名: article

| 字段名 | 类型 | 长度 | 允许空 | 默认值 | 注释 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| id | bigint | 否 | - | 文章主表：存储文章的核心数据 |
| title | character varying(200) | 否 | - | 文章主表：存储文章的核心数据 |
| sub_title | character varying(200) | 是 | - | 文章主表：存储文章的核心数据 |
| cover_image | character varying(500) | 是 | - | 文章主表：存储文章的核心数据 |
| summary | text | 是 | - | 文章主表：存储文章的核心数据 |
| content | text | 否 | - | 文章主表：存储文章的核心数据 |
| author_id | bigint | 否 | - | 文章主表：存储文章的核心数据 |
| category_id | bigint | 是 | - | 文章主表：存储文章的核心数据 |
| status | smallint | 是 | 0 | 文章主表：存储文章的核心数据 |
| view_count | bigint | 是 | 0 | 文章主表：存储文章的核心数据 |
| like_count | integer | 是 | 0 | 文章主表：存储文章的核心数据 |
| deleted | smallint | 否 | 0 | 文章主表：存储文章的核心数据 |
| create_time | timestamp without time zone | 是 | CURRENT_TIMESTAMP | 文章主表：存储文章的核心数据 |
| update_time | timestamp without time zone | 是 | CURRENT_TIMESTAMP | 文章主表：存储文章的核心数据 |
| version | integer | 是 | 0 | 文章主表：存储文章的核心数据 |

---

## 📋 表名: article_audit

| 字段名 | 类型 | 长度 | 允许空 | 默认值 | 注释 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| id | bigint | 否 | - | 文章审核记录表 |
| article_id | bigint | 否 | - | 文章审核记录表 |
| audit_status | smallint | 否 | 0 | 文章审核记录表 |
| audit_user_id | bigint | 是 | - | 文章审核记录表 |
| audit_remark | character varying(500) | 是 | NULL::character varying | 文章审核记录表 |
| audit_time | timestamp without time zone | 是 | - | 文章审核记录表 |
| submit_time | timestamp without time zone | 是 | - | 文章审核记录表 |
| create_time | timestamp without time zone | 是 | CURRENT_TIMESTAMP | 文章审核记录表 |
| update_time | timestamp without time zone | 是 | CURRENT_TIMESTAMP | 文章审核记录表 |

---

## 📋 表名: article_category

| 字段名 | 类型 | 长度 | 允许空 | 默认值 | 注释 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| id | bigint | 否 | - | 文章分类表：用于对文章进行归类管理 |
| category_name | character varying(50) | 否 | - | 文章分类表：用于对文章进行归类管理 |
| description | character varying(200) | 是 | - | 文章分类表：用于对文章进行归类管理 |
| sort_order | integer | 是 | 0 | 文章分类表：用于对文章进行归类管理 |
| status | smallint | 是 | 0 | 文章分类表：用于对文章进行归类管理 |
| deleted | smallint | 否 | 0 | 文章分类表：用于对文章进行归类管理 |
| create_time | timestamp without time zone | 是 | CURRENT_TIMESTAMP | 文章分类表：用于对文章进行归类管理 |
| update_time | timestamp without time zone | 是 | CURRENT_TIMESTAMP | 文章分类表：用于对文章进行归类管理 |
| version | integer | 是 | 0 | 文章分类表：用于对文章进行归类管理 |

---

## 📋 表名: article_tag

| 字段名 | 类型 | 长度 | 允许空 | 默认值 | 注释 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| id | bigint | 否 | - | 文章标签表：用于文章的灵活标记 |
| tag_name | character varying(30) | 否 | - | 文章标签表：用于文章的灵活标记 |
| status | smallint | 是 | 0 | 文章标签表：用于文章的灵活标记 |
| deleted | smallint | 否 | 0 | 文章标签表：用于文章的灵活标记 |
| create_time | timestamp without time zone | 是 | CURRENT_TIMESTAMP | 文章标签表：用于文章的灵活标记 |
| update_time | timestamp without time zone | 是 | CURRENT_TIMESTAMP | 文章标签表：用于文章的灵活标记 |

---

## 📋 表名: article_tag_ref

| 字段名 | 类型 | 长度 | 允许空 | 默认值 | 注释 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| article_id | bigint | 否 | - | 文章与标签的关联表：实现文章的多标签功能 |
| tag_id | bigint | 否 | - | 文章与标签的关联表：实现文章的多标签功能 |

---

## 📋 表名: sys_department

| 字段名 | 类型 | 长度 | 允许空 | 默认值 | 注释 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| id | bigint | 否 | - | 系统部门表 |
| organization_id | bigint | 否 | - | 系统部门表 |
| dept_name | character varying(100) | 否 | - | 系统部门表 |
| parent_id | bigint | 是 | 0 | 系统部门表 |
| level | integer | 是 | 1 | 系统部门表 |
| sort_order | integer | 是 | 0 | 系统部门表 |
| leader_id | bigint | 是 | - | 系统部门表 |
| phone | character varying(20) | 是 | - | 系统部门表 |
| email | character varying(50) | 是 | - | 系统部门表 |
| status | integer | 是 | 0 | 系统部门表 |
| deleted | integer | 是 | 0 | 系统部门表 |
| create_time | timestamp without time zone | 是 | CURRENT_TIMESTAMP | 系统部门表 |
| update_time | timestamp without time zone | 是 | CURRENT_TIMESTAMP | 系统部门表 |

---

## 📋 表名: sys_organization

| 字段名 | 类型 | 长度 | 允许空 | 默认值 | 注释 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| id | bigint | 否 | - | 系统单位表 |
| org_name | character varying(100) | 否 | - | 系统单位表 |
| org_code | character varying(50) | 否 | - | 系统单位表 |
| parent_id | bigint | 是 | 0 | 系统单位表 |
| level | integer | 是 | 1 | 系统单位表 |
| org_type | integer | 是 | 2 | 系统单位表 |
| is_system | integer | 是 | 0 | 系统单位表 |
| sort_order | integer | 是 | 0 | 系统单位表 |
| status | integer | 是 | 0 | 系统单位表 |
| description | character varying(500) | 是 | - | 系统单位表 |
| deleted | integer | 是 | 0 | 系统单位表 |
| create_time | timestamp without time zone | 是 | CURRENT_TIMESTAMP | 系统单位表 |
| update_time | timestamp without time zone | 是 | CURRENT_TIMESTAMP | 系统单位表 |

---

## 📋 表名: sys_permission

| 字段名 | 类型 | 长度 | 允许空 | 默认值 | 注释 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| id | bigint | 否 | - | 系统权限/菜单表 |
| parent_id | bigint | 否 | 0 | 系统权限/菜单表 |
| permission_name | character varying(50) | 否 | - | 系统权限/菜单表 |
| permission_code | character varying(100) | 是 | - | 系统权限/菜单表 |
| menu_type | integer | 否 | 1 | 系统权限/菜单表 |
| path | character varying(200) | 是 | - | 系统权限/菜单表 |
| component | character varying(200) | 是 | - | 系统权限/菜单表 |
| icon | character varying(50) | 是 | - | 系统权限/菜单表 |
| sort_order | integer | 是 | 0 | 系统权限/菜单表 |
| status | integer | 是 | 0 | 系统权限/菜单表 |
| deleted | integer | 是 | 0 | 系统权限/菜单表 |
| create_time | timestamp without time zone | 是 | - | 系统权限/菜单表 |
| update_time | timestamp without time zone | 是 | - | 系统权限/菜单表 |

---

## 📋 表名: sys_role

| 字段名 | 类型 | 长度 | 允许空 | 默认值 | 注释 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| id | bigint | 否 | - | 系统角色表 |
| role_name | character varying(50) | 否 | - | 系统角色表 |
| role_code | character varying(50) | 否 | - | 系统角色表 |
| description | character varying(200) | 是 | - | 系统角色表 |
| deleted | smallint | 否 | 0 | 系统角色表 |
| create_time | timestamp without time zone | 是 | CURRENT_TIMESTAMP | 系统角色表 |
| update_time | timestamp without time zone | 是 | CURRENT_TIMESTAMP | 系统角色表 |
| organization_id | bigint | 是 | - | 系统角色表 |

---

## 📋 表名: sys_role_permission

| 字段名 | 类型 | 长度 | 允许空 | 默认值 | 注释 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| role_id | bigint | 否 | - | 角色权限关联表 |
| permission_id | bigint | 否 | - | 角色权限关联表 |

---

## 📋 表名: sys_user

| 字段名 | 类型 | 长度 | 允许空 | 默认值 | 注释 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| id | bigint | 否 | - | 系统用户表 |
| username | character varying(50) | 否 | - | 系统用户表 |
| password | character varying(100) | 否 | - | 系统用户表 |
| email | character varying(100) | 是 | - | 系统用户表 |
| phone | character varying(20) | 是 | - | 系统用户表 |
| status | smallint | 是 | 0 | 系统用户表 |
| deleted | smallint | 否 | 0 | 系统用户表 |
| create_time | timestamp without time zone | 是 | CURRENT_TIMESTAMP | 系统用户表 |
| update_time | timestamp without time zone | 是 | CURRENT_TIMESTAMP | 系统用户表 |
| version | integer | 是 | 0 | 系统用户表 |
| organization_id | bigint | 是 | 1 | 系统用户表 |

---

## 📋 表名: sys_user_role

| 字段名 | 类型 | 长度 | 允许空 | 默认值 | 注释 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| user_id | bigint | 否 | - | 用户与角色关联表 |
| role_id | bigint | 否 | - | 用户与角色关联表 |
| organization_id | bigint | 是 | - | 用户与角色关联表 |

---

