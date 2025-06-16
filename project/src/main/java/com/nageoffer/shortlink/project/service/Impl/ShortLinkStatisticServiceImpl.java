package com.nageoffer.shortlink.project.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nageoffer.shortlink.project.dao.entity.LinkAccessLogsDO;
import com.nageoffer.shortlink.project.dao.entity.LinkAccessStatisticDO;
import com.nageoffer.shortlink.project.dao.entity.LinkDeviceStatisticDO;
import com.nageoffer.shortlink.project.dao.entity.LinkNetworkStatisticDO;
import com.nageoffer.shortlink.project.dao.mapper.*;
import com.nageoffer.shortlink.project.dto.req.ShortLinkStatisticAccessRecordReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkStatisticReqDTO;
import com.nageoffer.shortlink.project.dto.resp.*;
import com.nageoffer.shortlink.project.service.ShortLinkStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 短链接监控接口实现层
 */
@Service
@RequiredArgsConstructor
public class ShortLinkStatisticServiceImpl implements ShortLinkStatisticService {

    private final LinkAccessStatisticMapper linkAccessStatisticMapper;
//    private final LinkLocaleStatisticMapper linkLocaleStatisticMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkBrowserStatisticMapper linkBrowserStatisticMapper;
    private final LinkOsStatisticMapper linkOsStatisticMapper;
    private final LinkDeviceStatisticMapper linkDeviceStatisticMapper;
    private final LinkNetworkStatisticMapper linkNetworkStatisticMapper;

    @Override
    public ShortLinkStatisticRespDTO oneShortLinkStatistic(ShortLinkStatisticReqDTO requestParam) {
        // 基础访问详情
        List<LinkAccessStatisticDO> listStatisticByShortLink = linkAccessStatisticMapper.listStatisticByShortLink(requestParam);
        // 地区访问详情（仅国内）
        //不能获取高德地图API，故注释掉
//        List<ShortLinkStatisticLocaleCNRespDTO> localeCnStatistic = new ArrayList<>();
//        List<LinkLocaleStatisticDO> listedLocaleByShortLink = linkLocaleStatisticMapper.listLocaleByShortLink(requestParam);
//        int localeCnSum = listedLocaleByShortLink.stream()
//                .mapToInt(LinkLocaleStatisticDO::getCnt)
//                .sum();
//        listedLocaleByShortLink.forEach(each -> {
//            double ratio = (double) each.getCnt() / localeCnSum;
//            double actualRatio = Math.round(ratio * 100.0) / 100.0;
//            ShortLinkStatisticLocaleCNRespDTO localeCNRespDTO = ShortLinkStatisticLocaleCNRespDTO.builder()
//                    .cnt(each.getCnt())
//                    .locale(each.getProvince())
//                    .ratio(actualRatio)
//                    .build();
//            localeCnStatistic.add(localeCNRespDTO);
//        });
        // 小时访问详情
        List<Integer> hourStatistic = new ArrayList<>();
        List<LinkAccessStatisticDO> listHourStatisticByShortLink = linkAccessStatisticMapper.listHourStatisticByShortLink(requestParam);
        for (int i = 0; i < 24; i++) {
            AtomicInteger hour = new AtomicInteger(i);
            int hourCnt = listHourStatisticByShortLink.stream()
                    .filter(each -> Objects.equals(each.getHour(), hour.get()))
                    .findFirst()
                    .map(LinkAccessStatisticDO::getPv)
                    .orElse(0);
            hourStatistic.add(hourCnt);
        }
        //前端给出查询时间段；按小时汇总pv、uv等；然后返回一个integer列表，每个元素代表访问量，每个元素的索引对应小时。

        // 高频访问IP详情
        List<ShortLinkStatisticTopIpRespDTO> topIpStatistic = new ArrayList<>();
        List<HashMap<String, Object>> listTopIpByShortLink = linkAccessLogsMapper.listTopIpByShortLink(requestParam);
        listTopIpByShortLink.forEach(each -> {
            ShortLinkStatisticTopIpRespDTO statisticTopIpRespDTO = ShortLinkStatisticTopIpRespDTO.builder()
                    .ip(each.get("ip").toString())
                    .cnt(Integer.parseInt(each.get("count").toString()))
                    .build();
            topIpStatistic.add(statisticTopIpRespDTO);
        });
        //返回一个对象列表，每个对象包含两个属性，ip和对应的访问量

        // 一周访问详情
        List<Integer> weekdayStatistic = new ArrayList<>();
        List<LinkAccessStatisticDO> listWeekdayStatisticByShortLink = linkAccessStatisticMapper.listWeekdayStatisticByShortLink(requestParam);
        for (int i = 0; i < 7; i++) {
            AtomicInteger weekday = new AtomicInteger(i);
            int weekdayCnt = listWeekdayStatisticByShortLink.stream()
                    .filter(each -> Objects.equals(each.getWeekday(), weekday.get()))
                    .findFirst()
                    .map(LinkAccessStatisticDO::getPv)
                    .orElse(0);
            weekdayStatistic.add(weekdayCnt);
        }
        //返回一个integer列表，每个元素代表访问量，每个元素的索引对应星期几。

        // 浏览器访问详情
        List<ShortLinkStatisticBrowserRespDTO> browserStatistic = new ArrayList<>();
        List<HashMap<String, Object>> listBrowserStatisticByShortLink = linkBrowserStatisticMapper.listBrowserStatisticByShortLink(requestParam);
        int browserSum = listBrowserStatisticByShortLink.stream()
                .mapToInt(each -> Integer.parseInt(each.get("count").toString()))
                .sum();
        listBrowserStatisticByShortLink.forEach(each -> {
            double ratio = (double) Integer.parseInt(each.get("count").toString()) / browserSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkStatisticBrowserRespDTO browserRespDTO = ShortLinkStatisticBrowserRespDTO.builder()
                    .cnt(Integer.parseInt(each.get("count").toString()))
                    .browser(each.get("browser").toString())
                    .ratio(actualRatio)
                    .build();
            browserStatistic.add(browserRespDTO);
        });
        //返回一个浏览器对象列表，每个对象包含三个属性。

        // 操作系统访问详情
        List<ShortLinkStatisticOsRespDTO> osStatistic = new ArrayList<>();
        List<HashMap<String, Object>> listOsStatisticByShortLink = linkOsStatisticMapper.listOsStatisticByShortLink(requestParam);
        int osSum = listOsStatisticByShortLink.stream()
                .mapToInt(each -> Integer.parseInt(each.get("count").toString()))
                .sum();
        listOsStatisticByShortLink.forEach(each -> {
            double ratio = (double) Integer.parseInt(each.get("count").toString()) / osSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkStatisticOsRespDTO osRespDTO = ShortLinkStatisticOsRespDTO.builder()
                    .cnt(Integer.parseInt(each.get("count").toString()))
                    .os(each.get("os").toString())
                    .ratio(actualRatio)
                    .build();
            osStatistic.add(osRespDTO);
        });
        //返回一个操作系统对象列表，每个对象包含三个属性。

        // 访客访问类型详情
        List<ShortLinkStatisticUvRespDTO> uvTypeStatistic = new ArrayList<>();
        HashMap<String, Object> findUvTypeByShortLink = linkAccessLogsMapper.findUvTypeCntByShortLink(requestParam);
        int oldUserCnt = Integer.parseInt(findUvTypeByShortLink.get("oldUserCnt").toString());
        int newUserCnt = Integer.parseInt(findUvTypeByShortLink.get("newUserCnt").toString());
        int uvSum = oldUserCnt + newUserCnt;
        double oldRatio = (double) oldUserCnt / uvSum;
        double actualOldRatio = Math.round(oldRatio * 100.0) / 100.0;
        double newRatio = (double) newUserCnt / uvSum;
        double actualNewRatio = Math.round(newRatio * 100.0) / 100.0;
        ShortLinkStatisticUvRespDTO newUvRespDTO = ShortLinkStatisticUvRespDTO.builder()
                .uvType("newUser")
                .cnt(newUserCnt)
                .ratio(actualNewRatio)
                .build();
        uvTypeStatistic.add(newUvRespDTO);
        ShortLinkStatisticUvRespDTO oldUvRespDTO = ShortLinkStatisticUvRespDTO.builder()
                .uvType("oldUser")
                .cnt(oldUserCnt)
                .ratio(actualOldRatio)
                .build();
        uvTypeStatistic.add(oldUvRespDTO);
        //返回一个用户对象列表，列表包含两个对象，每个对象包含三个属性。

        // 访问设备类型详情
        List<ShortLinkStatisticDeviceRespDTO> deviceStatistic = new ArrayList<>();
        List<LinkDeviceStatisticDO> listDeviceStatisticByShortLink = linkDeviceStatisticMapper.listDeviceStatisticByShortLink(requestParam);
        int deviceSum = listDeviceStatisticByShortLink.stream()
                .mapToInt(LinkDeviceStatisticDO::getCnt)
                .sum();
        listDeviceStatisticByShortLink.forEach(each -> {
            double ratio = (double) each.getCnt() / deviceSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkStatisticDeviceRespDTO deviceRespDTO = ShortLinkStatisticDeviceRespDTO.builder()
                    .cnt(each.getCnt())
                    .device(each.getDevice())
                    .ratio(actualRatio)
                    .build();
            deviceStatistic.add(deviceRespDTO);
        });

        // 访问网络类型详情
        List<ShortLinkStatisticNetworkRespDTO> networkStatistic = new ArrayList<>();
        List<LinkNetworkStatisticDO> listNetworkStatisticByShortLink = linkNetworkStatisticMapper.listNetworkStatisticByShortLink(requestParam);
        int networkSum = listNetworkStatisticByShortLink.stream()
                .mapToInt(LinkNetworkStatisticDO::getCnt)
                .sum();
        listNetworkStatisticByShortLink.forEach(each -> {
            double ratio = (double) each.getCnt() / networkSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkStatisticNetworkRespDTO networkRespDTO = ShortLinkStatisticNetworkRespDTO.builder()
                    .cnt(each.getCnt())
                    .network(each.getNetwork())
                    .ratio(actualRatio)
                    .build();
            networkStatistic.add(networkRespDTO);
        });

        return ShortLinkStatisticRespDTO.builder()
                .daily(BeanUtil.copyToList(listStatisticByShortLink, ShortLinkStatisticAccessDailyRespDTO.class))
                //将DO对象转为DTO对象
                //.localeCnStatistic(localeCnStatistic)
                .hourStatistic(hourStatistic)
                .topIpStatistic(topIpStatistic)
                .weekdayStatistic(weekdayStatistic)
                .browserStatistic(browserStatistic)
                .osStatistic(osStatistic)
                .uvTypeStatistic(uvTypeStatistic)
                .deviceStatistic(deviceStatistic)
                .networkStatistic(networkStatistic)
                .build();
    }

    @Override
    public IPage<ShortLinkStatisticAccessRecordRespDTO> shortLinkStatisticAccessRecord(ShortLinkStatisticAccessRecordReqDTO requestParam) {
        LambdaQueryWrapper<LinkAccessLogsDO> queryWrapper = Wrappers.lambdaQuery(LinkAccessLogsDO.class)
                .eq(LinkAccessLogsDO::getGid, requestParam.getGid())
                .eq(LinkAccessLogsDO::getFullShortUrl, requestParam.getFullShortUrl())
                .between(LinkAccessLogsDO::getCreateTime, requestParam.getStartDate(), requestParam.getEndDate())
                .eq(LinkAccessLogsDO::getDelFlag, 0)
                .orderByDesc(LinkAccessLogsDO::getCreateTime);
        IPage<LinkAccessLogsDO> linkAccessLogsDOIPage = linkAccessLogsMapper.selectPage(requestParam, queryWrapper);
        // this requestParam object is used by selectPage to get the pagination parameters like-
        // the desired current page number and size (records per page).
        //.selectPage(requestParam, queryWrapper)的两个参数，前者定义分页查询逻辑，后者定义数据库过滤条件等
        IPage<ShortLinkStatisticAccessRecordRespDTO> actualResult = linkAccessLogsDOIPage.convert(each -> BeanUtil.toBean(each, ShortLinkStatisticAccessRecordRespDTO.class));
        List<String> userAccessLogsList = actualResult.getRecords().stream()
                .map(ShortLinkStatisticAccessRecordRespDTO::getUser)
                .toList();
        //.getRecords()是指The Data for the Current Page:即返回当前页的查询数据
        List<Map<String, Object>> uvTypeList = linkAccessLogsMapper.selectUvTypeByUsers(
                requestParam.getGid(),
                requestParam.getFullShortUrl(),
                requestParam.getStartDate(),
                requestParam.getEndDate(),
                userAccessLogsList
        );
        actualResult.getRecords().forEach(each -> {
            String uvType = uvTypeList.stream()
                    .filter(item -> Objects.equals(each.getUser(), item.get("user")))
                    .findFirst()
                    .map(item -> item.get("UvType"))
                    .map(Object::toString)
                    .orElse("旧访客");
            each.setUvType(uvType);
        });
        return actualResult;
    }
}
