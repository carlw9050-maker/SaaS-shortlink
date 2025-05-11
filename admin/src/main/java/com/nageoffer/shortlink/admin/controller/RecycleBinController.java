package com.nageoffer.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.common.convention.result.Results;
import com.nageoffer.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.nageoffer.shortlink.admin.remote.dto.req.RecycleBinAddReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.req.RecycleBinDeleteReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.req.RecycleBinRemoveReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.resp.ShortLinkPageResDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回收站管理控制层
 */
@RestController
@RequiredArgsConstructor
public class RecycleBinController {

    /**
     * TODO后续重构为SpringCloud Feign调用
     */

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {};

    @PostMapping("/api/shortlink/admin/v1/recycle-bin/add")
    public Result<Void> addRecycleBin(@RequestBody RecycleBinAddReqDTO requestParam){
        shortLinkRemoteService.addRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 分页查询回收站里的短链接
     */
    @GetMapping("/api/shortlink/admin/v1/recycle-bin/get-page")
    public Result<IPage<ShortLinkPageResDTO>> pageShortLink(ShortLinkPageReqDTO requestParam){
        //IPage<> 是 MyBatis-Plus 框架中定义的一个分页结果接口，用于封装分页查询的结果数据
        return shortLinkRemoteService.pageRecycleBinShortLink(requestParam);
    }

    /**
     *将短链接从回收站移出
     */
    @PostMapping("/api/shortlink/admin/v1/recycle-bin/remove")
    public Result<Void> addRecycleBin(@RequestBody RecycleBinRemoveReqDTO requestParam){
        shortLinkRemoteService.removeRecycleBin(requestParam);
        return Results.success();
    }

    /**
     *将回收站的短链接彻底删除
     */
    @PostMapping("/api/shortlink/admin/v1/recycle-bin/delete")
    public Result<Void> deleteRecycleBin(@RequestBody RecycleBinDeleteReqDTO requestParam){
        shortLinkRemoteService.deleteRecycleBin(requestParam);
        return Results.success();
    }
}
