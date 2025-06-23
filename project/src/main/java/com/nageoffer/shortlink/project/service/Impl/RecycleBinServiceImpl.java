package com.nageoffer.shortlink.project.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dao.mapper.ShortLinkMapper;
import com.nageoffer.shortlink.project.dto.req.RecycleBinAddReqDTO;
import com.nageoffer.shortlink.project.dto.req.RecycleBinDeleteReqDTO;
import com.nageoffer.shortlink.project.dto.req.RecycleBinRemoveReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageResDTO;
import com.nageoffer.shortlink.project.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static com.nageoffer.shortlink.project.common.constant.RedisKeyConstant.GOTO_IS_NULL_SHORT_LINK_KEY;
import static com.nageoffer.shortlink.project.common.constant.RedisKeyConstant.GOTO_SHORT_LINK_KEY;

/**
 * 回收站管理接口实现层
 */
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements RecycleBinService {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void addRecycleBin(RecycleBinAddReqDTO requestParam){
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus,0)
                .eq(ShortLinkDO::getDelFlag,0);
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .enableStatus(1)
                .build();
        baseMapper.update(shortLinkDO, updateWrapper);
        stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY,requestParam.getFullShortUrl()));
    }

    /**
     * 分页查询短链接
     */
    @Override
    public IPage<ShortLinkPageResDTO> pageRecycleBinShortLink(ShortLinkPageReqDTO requestParam){
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus,1)
                .eq(ShortLinkDO::getDelFlag,0)
                .orderByDesc(ShortLinkDO::getCreateTime);
        IPage<ShortLinkDO> resultPage=baseMapper.selectPage(requestParam, queryWrapper);
        //调用 baseMapper.selectPage 方法执行分页查询
        //传入分页参数 requestParam 和查询条件 queryWrapper
        //返回结果存储在 resultPage 中，类型是 IPage<ShortLinkDO>
        return resultPage.convert(each-> {
            ShortLinkPageResDTO result = BeanUtil.toBean(each,ShortLinkPageResDTO.class);
            result.setDomain("https://"+result.getDomain());
            return result;
        });
        //将查询结果 resultPage 中的每个 ShortLinkDO 对象转换为 ShortLinkPageResDTO 对象
        //使用 BeanUtil.toBean 进行对象属性拷贝,并且更新ShortLinkPageResDTO 对象的domain字段值，并返回转换后的分页结果
    }

    /**
     *将短链接从回收站移出
     */
    @Override
    public void removeRecycleBin(RecycleBinRemoveReqDTO requestParam){
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus,1)
                .eq(ShortLinkDO::getDelFlag,0);
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .enableStatus(0)
                .build();
        baseMapper.update(shortLinkDO, updateWrapper);
        stringRedisTemplate.delete(String.format(GOTO_IS_NULL_SHORT_LINK_KEY,requestParam.getFullShortUrl()));
    }

    /**
     *将回收站的短链接彻底删除
     */
    public void deleteRecycleBin(RecycleBinDeleteReqDTO requestParam){
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus,1)
                .eq(ShortLinkDO::getDelTime, 0L)
                .eq(ShortLinkDO::getDelFlag,0);
        ShortLinkDO delShortLinkDO = ShortLinkDO.builder()
                .delTime(System.currentTimeMillis())
                .build();
        delShortLinkDO.setDelFlag(1); //delFlag字段是设置在baseDO里，不是在ShortLinkDO里。
        baseMapper.update(delShortLinkDO, updateWrapper);
    }
    //对短链接执行彻底删除操作是：EnableStatus变为1，DelFlag变为1，然后给DelTime更新值
}
