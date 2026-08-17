drop table if exists `orm_user`;
create table `orm_user`
(
    `id`               int(11) NOT NULL AUTO INCREMENT PRIMARY KEY COMMENT '主键',
    `name`             VARCHAR(32) NOT NULL UNIQUE COMMENT '用户名',
    `password`         VARCHAR(32) NOT NULL COMMENT '加密后的密码',
    `salt`             VARCHAR(32) NOT NULL COMMENT '加密使用的盐',
    `email`            VARCHAR(32) NOT NULL UNIQUE COMMENT '邮箱',
    `phone_number`     VARCHAR(15) NOT NULL UNIQUE COMMENT '手机号码',
    `status`           INT(2) NOT NULL DEFAULT 1 COMMENT '状态，-1：逻辑删除，0：禁用，1：启用',
    `create_time`      DATETIME    NOT NULL DEFAULT NOW() COMMENT '创建时间',
    `last_login_time`  DATETIME             DEFAULT NULL COMMENT '上次登录时间',
    `last_update_time` DATETIME    NOT NULL DEFAULT NOW() COMMENT '上次更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Spring Boot Demo Orm 系列示例表';

INSERT INTO `orm_user`(`id`, `name`, `password`, `salt`, `email`, `phone_number`)
VALUES (1, 'user_1', 'ff342e862e7c3285cdc07e56d6b8973b', '412365a109674b2dbb1981ed561a4c70', 'user1@xkcoding.com',
        '17300000001');
INSERT INTO `orm_user`(`id`, `name`, `password`, `salt`, `email`, `phone_number`)
VALUES (2, 'user_2', '6c6bf02c8d5d3d128f34b1700cb1e32c', 'fcbdd0e8a9404a5585ea4e01d0e4d7a0', 'user2@xkcoding.com',
        '17300000002');