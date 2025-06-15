package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.LinkOsStatisticDO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkStatisticReqDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;

/**
 * 短链接访问之操作系统监控的持久层
 */
public interface LinkOsStatisticMapper extends BaseMapper<LinkOsStatisticDO> {
    /**
     * 记录操作系统访问数据
     */
    @Insert("INSERT INTO t_link_os_statistic (full_short_url,gid,date,cnt,os,create_time,update_time,del_flag)"+
            "VALUES(#{linkOsStatistic.fullShortUrl}, #{linkOsStatistic.gid}, #{linkOsStatistic.date}, #{linkOsStatistic.cnt}, #{linkOsStatistic.os}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE cnt = cnt + #{linkOsStatistic.cnt};")
    void shortLinkOsStatistic(@Param("linkOsStatistic") LinkOsStatisticDO linkOsStatisticDO);

    /**
     * 根据短链接获取指定日期内操作系统监控数据
     */
    @Select("SELECT " +
            "    os, " +
            "    SUM(cnt) AS count " +
            "FROM " +
            "    t_link_os_statistic " +
            "WHERE " +
            "    full_short_url = #{param.fullShortUrl} " +
            "    AND gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    full_short_url, gid, date, os;")
    List<HashMap<String, Object>> listOsStatisticByShortLink(@Param("param") ShortLinkStatisticReqDTO requestParam);
}
