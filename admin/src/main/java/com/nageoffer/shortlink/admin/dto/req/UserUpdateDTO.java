package com.nageoffer.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 用户持久层实体
 */
@Data   //lombok库提供的组合注解，自动生成getter/setter方法
public class UserUpdateDTO {
    /**
     * ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 真实姓名
     */
//    @TableField("real_name")
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String mail;
}