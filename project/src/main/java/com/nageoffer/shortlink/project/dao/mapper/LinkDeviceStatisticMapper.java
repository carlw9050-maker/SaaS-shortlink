package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.LinkDeviceStatisticDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

/**
 * 短链接访问监控-访问设备持久层
 */
public interface LinkDeviceStatisticMapper extends BaseMapper<LinkDeviceStatisticDO> {

    /**
     * 记录设备访问数据
     */
    @Insert("INSERT INTO t_link_device_statistic (full_short_url,gid,date,cnt,device,create_time,update_time,del_flag)"+
            "VALUES(#{linkDeviceStatistic.fullShortUrl}, #{linkDeviceStatistic.gid}, #{linkDeviceStatistic.date}, #{linkDeviceStatistic.cnt}, #{linkDeviceStatistic.device}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE cnt = cnt + #{linkDeviceStatistic.cnt};")
    void shortLinkDeviceStatistic(@Param("linkDeviceStatistic") LinkDeviceStatisticDO linkDeviceStatisticDO);
}
