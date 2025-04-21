package com.nageoffer.shortlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nageoffer.shortlink.admin.common.database.BaseDO;
import lombok.*;

/**
 * 短链接分组实体
 */
@Data
@TableName("t_group")
@Builder
@NoArgsConstructor
@AllArgsConstructor//这都是Lombok提供的注解，通常一起使用，使用后可以通过链式调用的方式创建对象
@EqualsAndHashCode(callSuper = true)  // 确保包含父类字段
public class GroupDO extends BaseDO {

    /**
     * ID
     */
    private Long id;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 分组名称
     */
    private String name;

    /**
     * 分组用户名
     */
    private String username;

    /**
     * 分组排序
     */
    private Integer sortOrder;


}
