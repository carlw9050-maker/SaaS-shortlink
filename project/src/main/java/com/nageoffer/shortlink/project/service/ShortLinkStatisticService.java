package com.nageoffer.shortlink.project.service;

import com.nageoffer.shortlink.project.dto.req.ShortLinkStatisticReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkStatisticRespDTO;

/**
 * 短链接监控接口层
 */
public interface ShortLinkStatisticService {

    /**
     * 获取单个短链接监控数据
     *
     * @param requestParam 获取短链接监控数据入参
     * @return 短链接监控数据
     */
    ShortLinkStatisticRespDTO oneShortLinkStatistic(ShortLinkStatisticReqDTO requestParam);
}

