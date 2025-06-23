package com.nageoffer.shortlink.project.test;

public class ShortLinkTableShardingTest {

    public static final String SQL="CREATE TABLE `t_link_%d` (\n" +
            //使用%d作为占位符，后面会被替换为分表编号
            "`id` bigint(20) unsigned zerofill NOT NULL AUTO_INCREMENT COMMENT 'ID',\n" +
            "`domain` varchar(128) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '域名',\n" +
            "`short_uri` varchar(8) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '短链接',\n" +
            "`full_short_url` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '完整短链接',\n" +
            "`origin_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '原始链接',\n" +
            "`click_num` int DEFAULT '0' COMMENT '原始信息',\n" +
            "`gid` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'default' COMMENT '短链接分组标识',\n" +
            "`favicon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '网站标识',\n" +
            "`enable_status` tinyint(1) DEFAULT NULL COMMENT '启用标识：0是已启用，1是未启用',\n" +
            "`created_type` tinyint(1) DEFAULT NULL COMMENT '创建类型：0是接口创建，1是控制台创建',\n" +
            "`valid_date_type` tinyint(1) DEFAULT NULL COMMENT '有效期类型：0：永久有效；1，自定义。',\n" +
            "`valid_date` datetime DEFAULT NULL COMMENT '有效期',\n" +
            "`describe` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '描述',\n" +
            "`total_pv` int(11) DEFAULT NULL COMMENT '历史pv访问',\n" +
            "`total_uv` int(11) DEFAULT NULL COMMENT '历史uv访问',\n" +
            "`total_uip` int(11) DEFAULT NULL COMMENT '历史uip访问',\n" +
            "`create_time` datetime DEFAULT NULL COMMENT '短链接创建时间',\n" +
            "`update_time` datetime DEFAULT NULL COMMENT '修改时间',\n" +
            "`del_time`        bigint(20) DEFAULT '0' COMMENT '删除时间戳', \n"+
            "`del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识：0表示未删除，1表示已删除',\n" +
            "PRIMARY KEY (`id`),\n" +
            "UNIQUE KEY `idx_unique_full_short-url` (`full_short_url`,`del_time`) USING BTREE\n" +
            ") ENGINE=InnoDB AUTO_INCREMENT=1915762985821798402 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;";
    public static void main(String[] args) {
        for (int i = 0; i < 16; i++) {
            System.out.printf((SQL)+"%n",i);
            //使用printf格式化输出SQL语句，将%d替换为当前循环的i值，%n表示换行符
        }
    }
}
//这段代码的作用是生成16个短链接分表的sql创建语句
