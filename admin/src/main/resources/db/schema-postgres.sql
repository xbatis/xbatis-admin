DROP DATABASE IF EXISTS xbatis_admin;
CREATE DATABASE xbatis_admin WITH ENCODING 'UTF8';
\connect xbatis_admin;

DROP TABLE IF EXISTS sys_role_menu;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_menu;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(120) NOT NULL,
    display_name VARCHAR(64) NOT NULL,
    email VARCHAR(128),
    phone VARCHAR(32),
    enabled INTEGER NOT NULL DEFAULT 1,
    remark VARCHAR(255),
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_role (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL UNIQUE,
    enabled INTEGER NOT NULL DEFAULT 1,
    sort INTEGER NOT NULL DEFAULT 0,
    deletable INTEGER NOT NULL DEFAULT 1,
    remark VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_menu (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(64) NOT NULL,
    type VARCHAR(16) NOT NULL,
    path VARCHAR(128),
    component VARCHAR(128),
    icon VARCHAR(64),
    permission VARCHAR(128),
    redirect VARCHAR(128),
    sort INTEGER NOT NULL DEFAULT 0,
    visible INTEGER NOT NULL DEFAULT 1,
    keep_alive INTEGER NOT NULL DEFAULT 0,
    status INTEGER NOT NULL DEFAULT 1,
    remark VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_user_role (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES sys_user(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES sys_role(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_sys_user_role UNIQUE (user_id, role_id)
);

CREATE TABLE sys_role_menu (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL REFERENCES sys_role(id) ON DELETE CASCADE,
    menu_id BIGINT NOT NULL REFERENCES sys_menu(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_sys_role_menu UNIQUE (role_id, menu_id)
);

INSERT INTO sys_role (id, name, enabled, sort, deletable, remark)
VALUES
    (1, '超级管理员', 1, 1, 0, '拥有全部资源权限'),
    (2, '运营管理员', 1, 2, 1, '用于演示受限权限账号');

INSERT INTO sys_menu (id, parent_id, name, type, path, component, icon, permission, redirect, sort, visible, keep_alive, status, remark)
VALUES
    (1, 0, '工作台', 'MENU', '/dashboard', 'dashboard', 'Burger', 'dashboard:view', '', 1, 1, 0, 1, '首页看板'),
    (10, 0, '系统管理', 'CATALOG', '/system', 'system', 'Setting', '', '/system/users', 10, 1, 0, 1, '权限中心根菜单'),
    (11, 10, '用户管理', 'MENU', '/system/users', 'system/users', 'Avatar', 'sys:user:view', '', 11, 1, 0, 1, '系统用户维护'),
    (12, 10, '角色管理', 'MENU', '/system/roles', 'system/roles', 'User', 'sys:role:view', '', 12, 1, 0, 1, '角色与菜单授权'),
    (13, 10, '菜单管理', 'MENU', '/system/menus', 'system/menus', 'Tickets', 'sys:menu:view', '', 13, 1, 0, 1, '路由与按钮权限维护'),
    (111, 11, '新增用户', 'BUTTON', NULL, NULL, NULL, 'sys:user:create', NULL, 1, 1, 0, 1, '创建用户'),
    (112, 11, '编辑用户', 'BUTTON', NULL, NULL, NULL, 'sys:user:update', NULL, 2, 1, 0, 1, '编辑用户'),
    (113, 11, '修改状态', 'BUTTON', NULL, NULL, NULL, 'sys:user:status', NULL, 3, 1, 0, 1, '启停用户'),
    (114, 11, '重置密码', 'BUTTON', NULL, NULL, NULL, 'sys:user:reset-password', NULL, 4, 1, 0, 1, '重置用户密码'),
    (121, 12, '新增角色', 'BUTTON', NULL, NULL, NULL, 'sys:role:create', NULL, 1, 1, 0, 1, '创建角色'),
    (122, 12, '编辑角色', 'BUTTON', '', '', '', 'sys:role:update', '', 2, 1, 0, 1, '编辑角色'),
    (123, 12, '删除角色', 'BUTTON', NULL, NULL, NULL, 'sys:role:delete', NULL, 3, 1, 0, 1, '删除角色'),
    (131, 13, '新增菜单', 'BUTTON', NULL, NULL, NULL, 'sys:menu:create', NULL, 1, 1, 0, 1, '创建菜单'),
    (132, 13, '编辑菜单', 'BUTTON', NULL, NULL, NULL, 'sys:menu:update', NULL, 2, 1, 0, 1, '编辑菜单'),
    (133, 13, '删除菜单', 'BUTTON', NULL, NULL, NULL, 'sys:menu:delete', NULL, 3, 1, 0, 1, '删除菜单');

INSERT INTO sys_user (id, username, password, display_name, email, phone, enabled, remark)
VALUES
    (1, 'admin', '$2a$10$13pvZP2ISUz/ZWnAWKv2suiIf7RwK76jYsB6ME7bbuQ81BX3MqvlW', '系统管理员', 'admin@example.com', '13800000000', 1, '默认超级管理员'),
    (2, 'ops', '$2a$10$13pvZP2ISUz/ZWnAWKv2suiIf7RwK76jYsB6ME7bbuQ81BX3MqvlW', '运营管理员', 'ops@example.com', '13900000000', 1, '默认演示账号');

INSERT INTO sys_user_role (user_id, role_id)
VALUES
    (1, 1),
    (2, 2);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES
    (1, 1),
    (1, 10),
    (1, 11),
    (1, 12),
    (1, 13),
    (1, 111),
    (1, 112),
    (1, 113),
    (1, 114),
    (1, 121),
    (1, 122),
    (1, 123),
    (1, 131),
    (1, 132),
    (1, 133),
    (2, 1),
    (2, 10),
    (2, 11);

SELECT setval('sys_role_id_seq', (SELECT MAX(id) FROM sys_role));
SELECT setval('sys_menu_id_seq', (SELECT MAX(id) FROM sys_menu));
SELECT setval('sys_user_id_seq', (SELECT MAX(id) FROM sys_user));
