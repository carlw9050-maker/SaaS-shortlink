package com.nageoffer.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.nageoffer.shortlink.admin.common.database.BaseDO;
import lombok.Data;

import java.util.Date;

/**
 * 短链接实体
 */
@TableName("t_link")
@Data
public class ShortLinkDO extends BaseDO {

    /**
     * ID
     */
    private Long id;

    /**
     * 域名
     */
    private String domain;

    /**
     * 短链接
     */
    private String shortUri;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 原始链接
     */
    private String originUrl;

    /**
     * 原始信息
     */
    private Integer clickNum;

    /**
     * 短链接分组标识
     */
    private String gid;

    /**
     * 启用标识：0是已启用，1是未启用
     */
    private Integer enableStatus;

    /**
     * 创建类型：0是接口创建，1是控制台创建
     */
    private Integer createdType;

    /**
     * 有效期类型：0：永久有效；1，自定义。
     */
    private Integer validDateType;

    /**
     * 有效期
     */
    private Date validDate;

    /**
     * 描述
     */
    @TableField("`describe`")
    private String describe;

}