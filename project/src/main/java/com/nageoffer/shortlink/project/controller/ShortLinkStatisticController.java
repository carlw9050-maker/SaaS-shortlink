package com.nageoffer.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.project.common.convention.result.Result;
import com.nageoffer.shortlink.project.common.convention.result.Results;
import com.nageoffer.shortlink.project.dto.req.ShortLinkGroupStatisticAccessRecordReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkGroupStatisticReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkStatisticAccessRecordReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkStatisticReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkStatisticAccessRecordRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkStatisticRespDTO;
import com.nageoffer.shortlink.project.service.ShortLinkStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接监控控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkStatisticController {

    private final ShortLinkStatisticService shortLinkStatisticService;

    /**
     * 访问单个短链接指定时间内监控数据（总体数据）
     */
    @GetMapping("/api/shortlink/v1/statistic")
    public Result<ShortLinkStatisticRespDTO> shortLinkStatistic(ShortLinkStatisticReqDTO requestParam) {
        return Results.success(shortLinkStatisticService.oneShortLinkStatistic(requestParam));
    }

    /**
     * 访问分组短链接指定时间内监控数据
     */
    @GetMapping("/api/shortlink/v1/statistic/group")
    public Result<ShortLinkStatisticRespDTO> groupShortLinkStatistic(ShortLinkGroupStatisticReqDTO requestParam) {
        return Results.success(shortLinkStatisticService.groupShortLinkStatistic(requestParam));
    }

    /**
     * 单个短链接指定时间内访问记录的分页查询（详细数据）
     */
    @GetMapping("/api/shortlink/v1/statistic/get-page")
    public Result<IPage<ShortLinkStatisticAccessRecordRespDTO>> shortLinkStatisticAccessRecord(ShortLinkStatisticAccessRecordReqDTO requestParam) {
        return Results.success(shortLinkStatisticService.shortLinkStatisticAccessRecord(requestParam));
    }

    /**
     * 分组短链接指定时间内访问记录的分页查询（详细数据）
     */
    @GetMapping("/api/shortlink/v1/statistic/group/get-page")
    public Result<IPage<ShortLinkStatisticAccessRecordRespDTO>> shortLinkGroupStatisticAccessRecord(ShortLinkGroupStatisticAccessRecordReqDTO requestParam) {
        return Results.success(shortLinkStatisticService.groupShortLinkStatisticAccessRecord(requestParam));
    }
}
