package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.LinkLocaleStatisticDO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkStatisticReqDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 短链接访问的地区监控的持久层
 */
public interface LinkLocaleStatisticMapper extends BaseMapper<LinkLocaleStatisticDO> {
    /**
     * 记录地区访问数据
     */
    @Insert("INSERT INTO t_link_locale_statistic (full_short_url,gid,date,cnt,province,city,adcode,country,create_time,update_time,del_flag)"+
            "VALUES(#{linkLocaleStatistic.fullShortUrl}, #{linkLocaleStatistic.gid}, #{linkLocaleStatistic.date}, #{linkLocaleStatistic.cnt}, #{linkLocaleStatistic.province}, #{linkLocaleStatistic.city}, #{linkLocaleStatistic.adcode}, #{linkLocaleStatistic.country}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE cnt = cnt + #{linkLocaleStatistic.cnt};")
    void shortLinkLocaleStatistic(@Param("linkLocaleStatistic") LinkLocaleStatisticDO linkLocaleStatisticDO);

    /**
     * 根据短链接获取指定日期内基础监控数据
     */
    @Select("SELECT " +
            "    province, " +
            "    SUM(cnt) AS cnt " +
            "FROM " +
            "    t_link_locale_statistic " +
            "WHERE " +
            "    full_short_url = #{param.fullShortUrl} " +
            "    AND gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    full_short_url, gid, province;")
    List<LinkLocaleStatisticDO> listLocaleByShortLink(@Param("param") ShortLinkStatisticReqDTO requestParam);
}
