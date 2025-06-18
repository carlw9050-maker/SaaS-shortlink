package com.nageoffer.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.nageoffer.shortlink.admin.remote.dto.req.ShortLinkGroupStatisticReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.req.ShortLinkStatisticAccessRecordReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.req.ShortLinkStatisticReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.resp.ShortLinkStatisticAccessRecordRespDTO;
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
     * 访问单个短链接指定时间内监控数据（总体数据）
     */
    @GetMapping("/api/shortlink/admin/v1/statistic")
    public Result<ShortLinkStatisticRespDTO> shortLinkStatistic(ShortLinkStatisticReqDTO requestParam) {
        return shortLinkRemoteService.oneShortLinkStatistic(requestParam);
    }

    /**
     * 访问分组短链接指定时间内监控数据
     */
    @GetMapping("/api/shortlink/admin/v1/statistic/group")
    public Result<ShortLinkStatisticRespDTO> groupShortLinkStatistic(ShortLinkGroupStatisticReqDTO requestParam) {
        return shortLinkRemoteService.groupShortLinkStatistic(requestParam);
    }

    /**
     * 单个短链接指定时间内访问记录的分页查询（详细数据）
     */
    @GetMapping("/api/shortlink/admin/v1/statistic/get-page")
    public Result<IPage<ShortLinkStatisticAccessRecordRespDTO>> shortLinkStatsAccessRecord(ShortLinkStatisticAccessRecordReqDTO requestParam) {
        return shortLinkRemoteService.shortLinkStatisticAccessRecord(requestParam);
    }
}
