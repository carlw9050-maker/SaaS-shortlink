package com.nageoffer.shortlink.project.dto.resp;

import lombok.Data;

/**
 * 短链接分组数量的查询请求的响应
 */
@Data
public class ShortLinkCountQueryRespDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 短链接数量
     */
    private Integer shortLinkCount;
}
