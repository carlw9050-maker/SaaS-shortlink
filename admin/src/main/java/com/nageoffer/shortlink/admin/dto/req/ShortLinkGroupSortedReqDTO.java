package com.nageoffer.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 短链接分组排序参数
 */
@Data
public class ShortLinkGroupSortedReqDTO {

    /**
     * 短链接分组标识
     */
    private String gid;

    /**
     * 短链接分组的顺序
     */
    private Integer sortOrder;
}
