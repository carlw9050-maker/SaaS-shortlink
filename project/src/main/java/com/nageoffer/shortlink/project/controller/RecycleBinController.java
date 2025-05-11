package com.nageoffer.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.project.common.convention.result.Result;
import com.nageoffer.shortlink.project.common.convention.result.Results;
import com.nageoffer.shortlink.project.dto.req.RecycleBinAddReqDTO;
import com.nageoffer.shortlink.project.dto.req.RecycleBinDeleteReqDTO;
import com.nageoffer.shortlink.project.dto.req.RecycleBinRemoveReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageResDTO;
import com.nageoffer.shortlink.project.service.RecycleBinService;
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

    private final RecycleBinService recycleBinService;

    /**
     * 将短链接添加至回收站
     */
    @PostMapping("/api/shortlink/v1/recycle-bin/add")
    public Result<Void> addRecycleBin(@RequestBody RecycleBinAddReqDTO requestParam){
        recycleBinService.addRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 分页查询回收站里的短链接
     */
    @GetMapping("/api/shortlink/v1/recycle-bin/get-page")
    public Result<IPage<ShortLinkPageResDTO>> pageShortLink(ShortLinkPageReqDTO requestParam){
        //IPage<> 是 MyBatis-Plus 框架中定义的一个分页结果接口，用于封装分页查询的结果数据
        return Results.success(recycleBinService.pageRecycleBinShortLink(requestParam));
    }

    /**
     * 将短链接从回收站移出
     */
    @PostMapping("/api/shortlink/v1/recycle-bin/remove")
    public Result<Void> removeRecycleBin(@RequestBody RecycleBinRemoveReqDTO requestParam){
        recycleBinService.removeRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 将回收站里的短链接彻底删除
     */
    @PostMapping("/api/shortlink/v1/recycle-bin/delete")
    public Result<Void> deleteRecycleBin(@RequestBody RecycleBinDeleteReqDTO requestParam){
        recycleBinService.deleteRecycleBin(requestParam);
        return Results.success();
    }
}
