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

 Date: 26/08/2026 11:36:07
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for orm_role
-- ----------------------------
DROP TABLE IF EXISTS `orm_role`;
CREATE TABLE `orm_role`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '角色名',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'Spring Boot Demo Orm 系列示例表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orm_role
-- ----------------------------
INSERT INTO `orm_role` VALUES (2, '普通用户');
INSERT INTO `orm_role` VALUES (1, '管理员');

SET FOREIGN_KEY_CHECKS = 1;
