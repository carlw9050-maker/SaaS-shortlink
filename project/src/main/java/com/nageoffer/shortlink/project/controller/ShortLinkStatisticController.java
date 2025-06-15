package com.nageoffer.shortlink.project.controller;

import com.nageoffer.shortlink.project.common.convention.result.Result;
import com.nageoffer.shortlink.project.common.convention.result.Results;
import com.nageoffer.shortlink.project.dto.req.ShortLinkStatisticReqDTO;
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
     * 访问单个短链接指定时间内监控数据
     */
    @GetMapping("/api/shortlink/v1/statistic")
    public Result<ShortLinkStatisticRespDTO> shortLinkStatistic(ShortLinkStatisticReqDTO requestParam) {
        return Results.success(shortLinkStatisticService.oneShortLinkStatistic(requestParam));
    }
}
