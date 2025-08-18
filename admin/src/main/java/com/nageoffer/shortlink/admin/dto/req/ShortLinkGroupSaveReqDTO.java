package com.nageoffer.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 短链接所存放分组
 */
@Data
public class ShortLinkGroupSaveReqDTO {

    /**
     * 短链接分组名称
     */
    private String name;
}
