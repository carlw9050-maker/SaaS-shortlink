package com.nageoffer.shortlink.admin.remote.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 短链接监控响应参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkStatisticRespDTO {

    /**
     * 访问量
     */
    private Integer pv;

    /**
     * 独立访客数
     */
    private Integer uv;

    /**
     * 独立IP数
     */
    private Integer uip;

    /**
     * 基础访问详情
     */
    private List<ShortLinkStatisticAccessDailyRespDTO> daily;

    /**
     * 地区访问详情（仅国内）
     */
    private List<ShortLinkStatisticLocaleCNRespDTO> localeCnStatistic;

    /**
     * 小时访问详情
     */
    private List<Integer> hourStatistic;

    /**
     * 高频访问IP详情
     */
    private List<ShortLinkStatisticTopIpRespDTO> topIpStatistic;

    /**
     * 一周访问详情
     */
    private List<Integer> weekdayStatistic;

    /**
     * 浏览器访问详情
     */
    private List<ShortLinkStatisticBrowserRespDTO> browserStatistic;

    /**
     * 操作系统访问详情
     */
    private List<ShortLinkStatisticOsRespDTO> osStatistic;

    /**
     * 访客访问类型详情
     */
    private List<ShortLinkStatisticUvRespDTO> uvTypeStatistic;

    /**
     * 访问设备类型详情
     */
    private List<ShortLinkStatisticDeviceRespDTO> deviceStatistic;

    /**
     * 访问网络类型详情
     */
    private List<ShortLinkStatisticNetworkRespDTO> networkStatistic;
}
