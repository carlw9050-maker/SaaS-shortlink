package com.nageoffer.shortlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;
/**
*用户持久层实体
*/
@Data   //lombok库提供的组合注解，自动生成getter/setter方法
@TableName("t_user")
public class UserDO {
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

    /**
     * 注销时间戳
     */
//    @TableField("deletion_time")
    private Long deletionTime;

    /**
     * 创建时间
     */
//    @TableField("creat_time")
    private Date creatTime;

    /**
     * 修改时间
     */
//    @TableField("update_time")
    private Date updateTime;

    /**
     * 删除标识 0：未删除 1：已删除
     */
//    @TableField("del_flag")
    private Integer delFlag;

}
