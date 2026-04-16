DROP DATABASE IF EXISTS xbatis_admin;
CREATE DATABASE xbatis_admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE xbatis_admin;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS sys_role_menu;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_menu;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(120) NOT NULL,
    display_name VARCHAR(64) NOT NULL,
    email VARCHAR(128) DEFAULT NULL,
    phone VARCHAR(32) DEFAULT NULL,
    enabled INT NOT NULL DEFAULT 1,
    remark VARCHAR(255) DEFAULT NULL,
    last_login_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    enabled INT NOT NULL DEFAULT 1,
    sort INT NOT NULL DEFAULT 0,
    deletable INT NOT NULL DEFAULT 1,
    remark VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_menu (
    id BIGINT NOT NULL AUTO_INCREMENT,
    parent_id BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(64) NOT NULL,
    type VARCHAR(16) NOT NULL,
    path VARCHAR(128) DEFAULT NULL,
    component VARCHAR(128) DEFAULT NULL,
    icon VARCHAR(64) DEFAULT NULL,
    permission VARCHAR(128) DEFAULT NULL,
    redirect VARCHAR(128) DEFAULT NULL,
    sort INT NOT NULL DEFAULT 0,
    visible INT NOT NULL DEFAULT 1,
    keep_alive INT NOT NULL DEFAULT 0,
    status INT NOT NULL DEFAULT 1,
    remark VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_role (user_id, role_id),
    KEY idx_sys_user_role_user_id (user_id),
    KEY idx_sys_user_role_role_id (role_id),
    CONSTRAINT fk_sys_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_sys_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_role_menu (
    id BIGINT NOT NULL AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_menu (role_id, menu_id),
    KEY idx_sys_role_menu_role_id (role_id),
    KEY idx_sys_role_menu_menu_id (menu_id),
    CONSTRAINT fk_sys_role_menu_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    CONSTRAINT fk_sys_role_menu_menu FOREIGN KEY (menu_id) REFERENCES sys_menu(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO sys_role (id, name, enabled, sort, deletable, remark)
VALUES
    (1, '超级管理员', 1, 1, 0, '拥有全部资源权限'),
    (2, '运营管理员', 1, 2, 1, '用于演示受限权限账号');

INSERT INTO sys_menu (id, parent_id, name, type, path, component, icon, permission, redirect, sort, visible, keep_alive, status, remark)
VALUES
    (1, 0, '工作台', 'MENU', '/dashboard', 'dashboard', 'DashboardOutlined', 'dashboard:view', NULL, 1, 1, 0, 1, '首页看板'),
    (10, 0, '系统管理', 'CATALOG', '/system', 'layout.blank', 'SettingOutlined', NULL, '/system/users', 10, 1, 0, 1, '权限中心根菜单'),
    (11, 10, '用户管理', 'MENU', '/system/users', 'system/users', 'TeamOutlined', 'sys:user:view', NULL, 11, 1, 0, 1, '系统用户维护'),
    (12, 10, '角色管理', 'MENU', '/system/roles', 'system/roles', 'SafetyCertificateOutlined', 'sys:role:view', NULL, 12, 1, 0, 1, '角色与菜单授权'),
    (13, 10, '菜单管理', 'MENU', '/system/menus', 'system/menus', 'ApartmentOutlined', 'sys:menu:view', NULL, 13, 1, 0, 1, '路由与按钮权限维护'),
    (111, 11, '新增用户', 'BUTTON', NULL, NULL, NULL, 'sys:user:create', NULL, 1, 1, 0, 1, '创建用户'),
    (112, 11, '编辑用户', 'BUTTON', NULL, NULL, NULL, 'sys:user:update', NULL, 2, 1, 0, 1, '编辑用户'),
    (113, 11, '修改状态', 'BUTTON', NULL, NULL, NULL, 'sys:user:status', NULL, 3, 1, 0, 1, '启停用户'),
    (114, 11, '重置密码', 'BUTTON', NULL, NULL, NULL, 'sys:user:reset-password', NULL, 4, 1, 0, 1, '重置用户密码'),
    (121, 12, '新增角色', 'BUTTON', NULL, NULL, NULL, 'sys:role:create', NULL, 1, 1, 0, 1, '创建角色'),
    (122, 12, '编辑角色', 'BUTTON', NULL, NULL, NULL, 'sys:role:update', NULL, 2, 1, 0, 1, '编辑角色'),
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

ALTER TABLE sys_role AUTO_INCREMENT = 3;
ALTER TABLE sys_menu AUTO_INCREMENT = 134;
ALTER TABLE sys_user AUTO_INCREMENT = 3;

SET FOREIGN_KEY_CHECKS = 1;
