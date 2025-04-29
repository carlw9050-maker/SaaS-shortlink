package com.nageoffer.shortlink.admin.test;

public class UserTableShardingTest {

//    public static final String SQL="CREATE TABLE `t_user_%d` (\n" +
//            使用%d作为占位符，后面会被替换为分表编号
//            "  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',\n" +
//            "  `username` varchar(256) DEFAULT NULL COMMENT '用户名',\n" +
//            "  `password` varchar(512) DEFAULT NULL COMMENT '密码',\n" +
//            "  `real_name` varchar(256) DEFAULT NULL COMMENT '真实姓名',\n" +
//            "  `phone` varchar(128) DEFAULT NULL COMMENT '手机号',\n" +
//            "  `mail` varchar(512) DEFAULT NULL COMMENT '邮箱',\n" +
//            "  `deletion_time` bigint(20) DEFAULT NULL COMMENT '注销时间戳',\n" +
//            "  `creat_time` datetime DEFAULT NULL COMMENT '创建时间',\n" +
//            "  `update_time` datetime DEFAULT NULL COMMENT '修改时间',\n" +
//            "  `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',\n" +
//            "  PRIMARY KEY (`id`)\n" +
//            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
            public static final String SQL="CREATE TABLE `t_group_%d` (\n" +
                "`id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',\n" +
                "`gid` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '分组标识',\n" +
                "`name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '分组名称',\n" +
                "`username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建分组用户',\n" +
                "`sort_order` int DEFAULT NULL COMMENT '分组排序',\n" +
                "`creat_time` datetime DEFAULT NULL COMMENT '创建时间',\n" +
                "`update_time` datetime DEFAULT NULL COMMENT '更新时间',\n" +
                "`del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识',\n" +
                "PRIMARY KEY (`id`) USING BTREE,\n" +
                "UNIQUE KEY `idx_unique_gid_username` (`gid`,`username`) USING BTREE\n" +
                ") ENGINE=InnoDB AUTO_INCREMENT=1914547892853444610 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;";
    public static void main(String[] args) {
        for (int i = 0; i < 16; i++) {
            System.out.printf((SQL)+"%n",i);
            //使用printf格式化输出SQL语句，将%d替换为当前循环的i值，%n表示换行符
        }
    }
}
//这段代码的作用是生成16个用户分表的sql创建语句