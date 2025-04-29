package com.nageoffer.shortlink.admin.remote.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor//这都是Lombok提供的注解，通常一起使用，使用后可以通过链式调用的方式创建对象
public class ShortLinkCreateRespDTO {

    /**
     * 原始链接
     */
    private String originUrl;


    /**
     * 短链接分组标识
     */
    private String gid;

    /**
     * 短链接全信息
     */
    private String fullShortUrl;
}
