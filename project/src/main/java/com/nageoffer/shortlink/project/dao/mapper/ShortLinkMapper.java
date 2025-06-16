package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkStatisticReqDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 短链接持久层
 */
public interface ShortLinkMapper extends BaseMapper<ShortLinkDO> {

    /**
     * 短链接访问统计自增
     */
    @Update("update t_link set total_pv = total_pv + #{totalPv}, total_uv = total_uv + #{totalUv}, total_uip = total_uip + #{totalUip} where gid = #{gid} and full_short_url = #{fullShortUrl}")
    void incrementStats(
            @Param("gid") String gid,
            @Param("fullShortUrl") String fullShortUrl,
            @Param("totalPv") Integer totalPv,
            @Param("totalUv") Integer totalUv,
            @Param("totalUip") Integer totalUip
    );

    /**
     * 查询total_pv, total_uv, total_uip的字段值
     * @param requestParam
     * @return
     */
    @Select("SELECT total_pv, total_uv, total_uip FROM t_link WHERE gid = #{param.gid} AND full_short_url = #{param.fullShortUrl}")
    ShortLinkDO getSumAccess(@Param("param") ShortLinkStatisticReqDTO requestParam);

}
