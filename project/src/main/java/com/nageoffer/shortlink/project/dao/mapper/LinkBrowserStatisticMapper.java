package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.LinkBrowserStatisticDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

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
}
