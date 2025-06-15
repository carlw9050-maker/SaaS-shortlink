package com.nageoffer.shortlink.admin.controller;

import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.nageoffer.shortlink.admin.remote.dto.req.ShortLinkStatisticReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.resp.ShortLinkStatisticRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接监控控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkStatisticController {

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {};

    /**
     * 访问单个短链接指定时间内监控数据
     */
    @GetMapping("/api/shortlink/admin/v1/statistic")
    public Result<ShortLinkStatisticRespDTO> shortLinkStatistic(ShortLinkStatisticReqDTO requestParam) {
        return shortLinkRemoteService.oneShortLinkStatistic(requestParam);
    }
}
