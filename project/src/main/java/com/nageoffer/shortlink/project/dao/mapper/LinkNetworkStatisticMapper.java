package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.LinkNetworkStatisticDO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkGroupStatisticReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkStatisticReqDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 短链接访问监控-访问设备持久层
 */
public interface LinkNetworkStatisticMapper extends BaseMapper<LinkNetworkStatisticDO> {

    /**
     * 记录设备访问数据
     */
    @Insert("INSERT INTO t_link_network_statistic (full_short_url,gid,date,cnt,network,create_time,update_time,del_flag)"+
            "VALUES(#{linkNetworkStatistic.fullShortUrl}, #{linkNetworkStatistic.gid}, #{linkNetworkStatistic.date}, #{linkNetworkStatistic.cnt}, #{linkNetworkStatistic.network}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE cnt = cnt + #{linkNetworkStatistic.cnt};")
    void shortLinkNetworkStatistic(@Param("linkNetworkStatistic") LinkNetworkStatisticDO linkNetworkStatisticDO);

    /**
     * 根据单个短链接获取指定日期内访问网络监控数据
     */
    @Select("SELECT " +
            "    network, " +
            "    SUM(cnt) AS cnt " +
            "FROM " +
            "    t_link_network_statistic " +
            "WHERE " +
            "    full_short_url = #{param.fullShortUrl} " +
            "    AND gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    full_short_url, gid, network;")
    List<LinkNetworkStatisticDO> listNetworkStatisticByShortLink(@Param("param") ShortLinkStatisticReqDTO requestParam);

    /**
     * 根据分组短链接获取指定日期内访问网络监控数据
     */
    @Select("SELECT " +
            "    network, " +
            "    SUM(cnt) AS cnt " +
            "FROM " +
            "    t_link_network_statistic " +
            "WHERE " +
            "    gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    gid, network;")
    List<LinkNetworkStatisticDO> listNetworkStatisticByGroup(@Param("param") ShortLinkGroupStatisticReqDTO requestParam);
}
