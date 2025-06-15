package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.LinkAccessStatisticDO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkStatisticReqDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 短链接访问监控持久层
 */
public interface LinkAccessStatisticMapper extends BaseMapper<LinkAccessStatisticDO> {

    @Insert("INSERT INTO t_link_access_statistic (full_short_url,gid,date,pv,uv,uip,hour,weekday,create_time,update_time,del_flag)"+
            "VALUES(#{linkAccessStatistic.fullShortUrl}, #{linkAccessStatistic.gid}, #{linkAccessStatistic.date}, #{linkAccessStatistic.pv}, #{linkAccessStatistic.uv}, #{linkAccessStatistic.uip}, #{linkAccessStatistic.hour}, #{linkAccessStatistic.weekday}, NOW(), NOW(), 0) ON DUPLICATE KEY UPDATE pv = pv + #{linkAccessStatistic.pv}," +
            "uv = uv + #{linkAccessStatistic.uv}," +
            "uip = uip + #{linkAccessStatistic.uip};")
    void shortLinkStatistic(@Param("linkAccessStatistic") LinkAccessStatisticDO linkAccessStatisticDO);

    /**
     * 根据短链接获取指定日期内基础监控数据
     */
    @Select("SELECT " +
            "    date, " +
            "    SUM(pv) AS pv, " +
            "    SUM(uv) AS uv, " +
            "    SUM(uip) AS uip " +
            "FROM " +
            "    t_link_access_statistic " +
            "WHERE " +
            "    full_short_url = #{param.fullShortUrl} " +
            "    AND gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    full_short_url, gid, date;")
    List<LinkAccessStatisticDO> listStatisticByShortLink(@Param("param") ShortLinkStatisticReqDTO requestParam);

    /**
     * 根据短链接获取指定日期内小时基础监控数据
     */
    @Select("SELECT " +
            "    hour, " +
            "    SUM(pv) AS pv " +
            "FROM " +
            "    t_link_access_statistic " +
            "WHERE " +
            "    full_short_url = #{param.fullShortUrl} " +
            "    AND gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    full_short_url, gid, hour;")
    List<LinkAccessStatisticDO> listHourStatisticByShortLink(@Param("param") ShortLinkStatisticReqDTO requestParam);

    /**
     * 根据短链接获取指定日期内星期几-基础监控数据
     */
    @Select("SELECT " +
            "    weekday, " +
            "    SUM(pv) AS pv " +
            "FROM " +
            "    t_link_access_statistic " +
            "WHERE " +
            "    full_short_url = #{param.fullShortUrl} " +
            "    AND gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    full_short_url, gid, weekday;")
    List<LinkAccessStatisticDO> listWeekdayStatisticByShortLink(@Param("param") ShortLinkStatisticReqDTO requestParam);
}
