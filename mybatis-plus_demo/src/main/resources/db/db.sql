DROP TABLE IF EXISTS user;

CREATE TABLE `t_user` (
                          `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                          `name` varchar(30) NOT NULL DEFAULT '' COMMENT '姓名',
                          `age` int(11) NULL DEFAULT NULL COMMENT '年龄',
                          `gender` tinyint(2) NOT NULL DEFAULT 0 COMMENT '性别，0：女 1：男',
                          PRIMARY KEY (`id`)
) COMMENT = '用户表';

INSERT INTO `t_user` (`id`, `name`, `age`, `gender`) VALUES (1, '犬小哈', 30, 1);
INSERT INTO `t_user` (`id`, `name`, `age`, `gender`) VALUES (2, '关羽', 46, 1);
INSERT INTO `t_user` (`id`, `name`, `age`, `gender`) VALUES (3, '诸葛亮', 26, 1);


CREATE TABLE `t_order` (
                           `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                           `order_id` bigint(20) UNSIGNED NOT NULL COMMENT '订单ID',
                           `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '下单用户ID',
                           `goods_name` varchar(30) NOT NULL COMMENT '商品名称',
                           `goods_price` decimal(10,2) NOT NULL COMMENT '商品价格',
                           PRIMARY KEY (`id`),
                           INDEX idx_order_id(`order_id`)
) COMMENT = '订单表';

INSERT INTO `t_order` (`id`, `order_id`, `user_id`, `goods_name`, `goods_price`) VALUES (1, 805646264648356, 1, 'Switch 游戏机', 1400.00);
INSERT INTO `t_order` (`id`, `order_id`, `user_id`, `goods_name`, `goods_price`) VALUES (2, 551787441310504, 1, '小米手机', 2000.00);
INSERT INTO `t_order` (`id`, `order_id`, `user_id`, `goods_name`, `goods_price`) VALUES (3, 938562101633493, 2, '《三国演义》', 66.00);
INSERT INTO `t_order` (`id`, `order_id`, `user_id`, `goods_name`, `goods_price`) VALUES (4, 791129917310894, 3, '华为手机', 1200.00);
INSERT INTO `t_order` (`id`, `order_id`, `user_id`, `goods_name`, `goods_price`) VALUES (5, 208722395587361, 3, '《西游记》', 56.00);
