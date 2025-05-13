package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.LinkOsStatisticDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

/**
 * 短链接访问之浏览器监控的持久层
 */
public interface LinkOsStatisticMapper extends BaseMapper<LinkOsStatisticDO> {
    /**
     * 记录浏览器访问数据
     */
    @Insert("INSERT INTO t_link_os_statistic (full_short_url,gid,date,cnt,os,create_time,update_time,del_flag)"+
            "VALUES(#{linkOsStatistic.fullShortUrl}, #{linkOsStatistic.gid}, #{linkOsStatistic.date}, #{linkOsStatistic.cnt}, #{linkOsStatistic.os}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE cnt = cnt + #{linkOsStatistic.cnt};")
    void shortLinkOsStatistic(@Param("linkOsStatistic") LinkOsStatisticDO linkOsStatisticDO);
}
