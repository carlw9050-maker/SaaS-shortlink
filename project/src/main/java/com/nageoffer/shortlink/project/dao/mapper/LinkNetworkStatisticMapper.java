package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.LinkNetworkStatisticDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

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
}
