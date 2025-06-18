package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.LinkBrowserStatisticDO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkGroupStatisticReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkStatisticReqDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;

/**
 * 短链接访问之浏览器监控的持久层
 */
public interface LinkBrowserStatisticMapper extends BaseMapper<LinkBrowserStatisticDO> {
    /**
     * 记录浏览器访问数据
     */
    @Insert("INSERT INTO t_link_browser_statistic (full_short_url,gid,date,cnt,browser,create_time,update_time,del_flag)"+
            "VALUES(#{linkBrowserStatistic.fullShortUrl}, #{linkBrowserStatistic.gid}, #{linkBrowserStatistic.date}, #{linkBrowserStatistic.cnt}, #{linkBrowserStatistic.browser}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE cnt = cnt + #{linkBrowserStatistic.cnt};")
    void shortLinkBrowserStatistic(@Param("linkBrowserStatistic") LinkBrowserStatisticDO linkBrowserStatisticDO);

    /**
     * 根据单个短链接获取指定日期内浏览器监控数据
     */
    @Select("SELECT " +
            "    browser, " +
            "    SUM(cnt) AS count " +
            "FROM " +
            "    t_link_browser_statistic " +
            "WHERE " +
            "    full_short_url = #{param.fullShortUrl} " +
            "    AND gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    full_short_url, gid, date, browser;")
    List<HashMap<String, Object>> listBrowserStatisticByShortLink(@Param("param") ShortLinkStatisticReqDTO requestParam);

    /**
     * 根据分组短链接获取指定日期内浏览器监控数据
     */
    @Select("SELECT " +
            "    browser, " +
            "    SUM(cnt) AS count " +
            "FROM " +
            "    t_link_browser_statistic " +
            "WHERE " +
            "    gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    gid, date, browser;")
    List<HashMap<String, Object>> listBrowserStatisticByGroup(@Param("param") ShortLinkGroupStatisticReqDTO requestParam);
}
