package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.LinkAccessStatisticDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

/**
 * 短链接访问监控持久层
 */
public interface LinkAccessStatisticMapper extends BaseMapper<LinkAccessStatisticDO> {

    @Insert("INSERT INTO t_link_access_statistic (full_short_url,gid,date,pv,uv,uip,hour,weekday,create_time,update_time,del_flag)"+
            "VALUES(#{linkAccessStatistic.fullShortUrl}, #{linkAccessStatistic.gid}, #{linkAccessStatistic.date}, #{linkAccessStatistic.pv}, #{linkAccessStatistic.uv}, #{linkAccessStatistic.uip}, #{linkAccessStatistic.hour}, #{linkAccessStatistic.weekday}, NOW(), NOW(), 0) ON DUPLICATE KEY UPDATE pv = pv + #{linkAccessStatistic.pv}," +
            "uv = uv + #{linkAccessStatistic.uv}," +
            "uip = uip + #{linkAccessStatistic.uip};")
    void shortLinkStatistic(@Param("linkAccessStatistic") LinkAccessStatisticDO linkAccessStatisticDO);
}
