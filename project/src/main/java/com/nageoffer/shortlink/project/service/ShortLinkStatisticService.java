package com.nageoffer.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.project.dto.req.ShortLinkGroupStatisticAccessRecordReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkGroupStatisticReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkStatisticAccessRecordReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkStatisticReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkStatisticAccessRecordRespDTO;
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

    /**
     * 分页查询单个短链接指定时间内的访问记录
     *
     * @param requestParam 获取短链接监控访问记录数据入参
     * @return 访问记录监控数据
     */
    IPage<ShortLinkStatisticAccessRecordRespDTO> shortLinkStatisticAccessRecord(ShortLinkStatisticAccessRecordReqDTO requestParam);

    /**
     * 获取分组短链接监控数据
     *
     * @param requestParam 获取分组短链接监控数据入参
     * @return 分组短链接监控数据
     */
    ShortLinkStatisticRespDTO groupShortLinkStatistic(ShortLinkGroupStatisticReqDTO requestParam);

    /**
     * 获取分组短链接监控数据
     *
     * @param requestParam 获取分组短链接监控数据入参
     * @return 分组短链接监控数据
     */
    IPage<ShortLinkStatisticAccessRecordRespDTO> groupShortLinkStatisticAccessRecord(ShortLinkGroupStatisticAccessRecordReqDTO requestParam);
}

