package com.nageoffer.shortlink.admin.controller;

import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.common.convention.result.Results;
import com.nageoffer.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.nageoffer.shortlink.admin.remote.dto.req.RecycleBinAddReqDTO;
import lombok.RequiredArgsConstructor;
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
}
