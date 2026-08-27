/*
 Navicat Premium Data Transfer

 Source Server         : 本地链接
 Source Server Type    : MySQL
 Source Server Version : 50733
 Source Host           : localhost:3306
 Source Schema         : furns_ssm

 Target Server Type    : MySQL
 Target Server Version : 50733
 File Encoding         : 65001

 Date: 26/08/2026 11:35:59
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for orm_user
-- ----------------------------
DROP TABLE IF EXISTS `orm_user`;
CREATE TABLE `orm_user`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '加密后的密码',
  `salt` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '加密使用的盐',
  `email` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '邮箱',
  `phone_number` varchar(15) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '手机号码',
  `status` int(2) NOT NULL DEFAULT 1 COMMENT '状态，-1：逻辑删除，0：禁用，1：启用',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `last_login_time` datetime(0) NULL DEFAULT NULL COMMENT '上次登录时间',
  `last_update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '上次更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`name`) USING BTREE,
  UNIQUE INDEX `email`(`email`) USING BTREE,
  UNIQUE INDEX `phone_number`(`phone_number`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'Spring Boot Demo Orm 系列示例表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orm_user
-- ----------------------------
INSERT INTO `orm_user` VALUES (1, 'user_1', 'ff342e862e7c3285cdc07e56d6b8973b', '412365a109674b2dbb1981ed561a4c70', 'user1@xkcoding.com', '17300000001', 1, '2026-08-17 14:04:24', NULL, '2026-08-17 14:04:24');
INSERT INTO `orm_user` VALUES (2, 'user_2', '6c6bf02c8d5d3d128f34b1700cb1e32c', 'fcbdd0e8a9404a5585ea4e01d0e4d7a0', 'user2@xkcoding.com', '17300000002', 1, '2026-08-17 14:04:24', NULL, '2026-08-17 14:04:24');
INSERT INTO `orm_user` VALUES (3, '小明', '4b362973ce4a9eb944226bc3fabd9069', 'aa1d3bede2cb4ac1871370361c4bf23a', '10086@qq.com', '10086', 1, '2026-08-19 13:44:26', NULL, '2026-08-19 13:44:26');
INSERT INTO `orm_user` VALUES (4, '小花', 'a37101ed8ace796d2c876aeb8092d50e', 'f02a11d80e684076ae4f4ecb4462ab64', '10087@qq.com', '10087', 1, '2026-08-21 16:27:36', NULL, '2026-08-21 16:27:36');

SET FOREIGN_KEY_CHECKS = 1;
