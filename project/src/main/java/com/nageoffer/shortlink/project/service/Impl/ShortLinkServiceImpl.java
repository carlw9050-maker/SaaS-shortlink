package com.nageoffer.shortlink.project.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrBuilder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.common.convention.exception.ClientException;
import com.nageoffer.shortlink.project.common.convention.exception.ServiceException;
import com.nageoffer.shortlink.project.common.enums.ValidDateTypeEnum;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dao.mapper.ShortLinkMapper;
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCountQueryRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageResDTO;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import com.nageoffer.shortlink.project.toolkit.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 短链接接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;
    //该字段与方法名称一致，是一种约定做法，这样spring会自动将由@Bean注解的方法创建的Bean注入到该字段中

    /**
     * 创建短链接
     */
    @Override
    public ShortLinkCreateRespDTO creatShortLink(ShortLinkCreateReqDTO requestParam) {
        String shortLinkSuffix = generateSuffix(requestParam);
        String fullShortUrl = StrBuilder.create(requestParam.getDomain())
                .append("/").append(shortLinkSuffix)
                .toString();
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(requestParam.getDomain())
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .createdType(requestParam.getCreatedType())
                .validDate(requestParam.getValidDate())
                .validDateType(requestParam.getValidDateType())
                .describe(requestParam.getDescribe())
                .shortUri(shortLinkSuffix)
                .enableStatus(0)
                .fullShortUrl(fullShortUrl)
                .build();
        try {
            baseMapper.insert(shortLinkDO);
        }catch (DuplicateKeyException ex){
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getShortUri, fullShortUrl);
            ShortLinkDO hashShortLink = baseMapper.selectOne(queryWrapper);
            if(hashShortLink != null){
                log.warn("短链接：{} 重复入库",fullShortUrl);
                throw new ServiceException("短链接生成重复");
            }
        }
        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl(shortLinkDO.getFullShortUrl())
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .build();
    }

    /**
     * 分页查询短链接
     */
    @Override
    public IPage<ShortLinkPageResDTO> pageShortLink(ShortLinkPageReqDTO requestParam){
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus,0)
                .eq(ShortLinkDO::getDelFlag,0)
                .orderByDesc(ShortLinkDO::getCreatTime);
        IPage<ShortLinkDO> resultPage=baseMapper.selectPage(requestParam, queryWrapper);
        //调用 baseMapper.selectPage 方法执行分页查询
        //传入分页参数 requestParam 和查询条件 queryWrapper
        //返回结果存储在 resultPage 中，类型是 IPage<ShortLinkDO>
        return resultPage.convert(each-> BeanUtil.toBean(each,ShortLinkPageResDTO.class));
        //将查询结果 resultPage 中的每个 ShortLinkDO 对象转换为 ShortLinkPageResDTO 对象
        //使用 BeanUtil.toBean 进行对象属性拷贝,返回转换后的分页结果
    }

    /**
     * 查询每个分组中短链接数量
     */
    @Override
    public List<ShortLinkCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam){
        QueryWrapper<ShortLinkDO> queryWrapper = Wrappers.query(new  ShortLinkDO())
                .select("gid,count(*) as shortLinkCount")//gid：表示选择查询结果中包含 gid 这个字段（列）的记录；count(*) as shortLinkCount"：表示对每组记录进行计数（统计行数）并映射给字段
                .in("gid",requestParam)//查询 gid 字段在 requestParam 列表中的记录。
                .eq("enable_status",0)//查询 enable_status 字段等于 0 的记录
                .groupBy("gid");//按 gid 字段分组，分组后，相同 gid 的记录会被合并，count(*) 会计算每组的记录数
        List<Map<String,Object>> shortLinkDOList =  baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(shortLinkDOList,ShortLinkCountQueryRespDTO.class);
    }

    private String generateSuffix(ShortLinkCreateReqDTO requestParam) {
        int customGenerateCount = 0;
        String shortUri;
        while (true) {
            if (customGenerateCount > 10) {
                throw new ServiceException("短链接频繁生成，请稍后再试");
            }
            String orginUrl = requestParam.getOriginUrl();
            orginUrl += System.currentTimeMillis();
            shortUri = HashUtil.hashToBase62(orginUrl);
            if(!shortUriCreateCachePenetrationBloomFilter.contains(requestParam.getDomain() + "/" + shortUri)){
                break;
            }
            customGenerateCount++;
        }
        return shortUri;
    }

    /**
     * 新增短链接
     * TODO 仅仅是考虑到了gid不变时的变更，后续还需做调整
     */
    @Transactional(rollbackFor = Exception.class)
    //方法在抛出任何异常时回滚，保证“删除+插入”操作的原子性。
    @Override
    public void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid,requestParam.getGid())
                .eq(ShortLinkDO::getFullShortUrl,requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getDelFlag,0)
                .eq(ShortLinkDO::getEnableStatus,0);
        ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
        if(hasShortLinkDO == null){
            throw new ClientException("短链接记录不存在");
        }
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(hasShortLinkDO.getDomain())
                .shortUri(hasShortLinkDO.getShortUri())
                .clickNum(hasShortLinkDO.getClickNum())
                .favicon(hasShortLinkDO.getFavicon())
                .createdType(hasShortLinkDO.getCreatedType())
                .gid(requestParam.getGid())
                .originUrl(requestParam.getOriginUrl())
                .describe(requestParam.getDescribe())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .build();
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl,requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getGid,requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus,0)
                .eq(ShortLinkDO::getDelFlag,0)
                .set(Objects.equals(requestParam.getValidDateType(), ValidDateTypeEnum.PERNAMENT.getType()),ShortLinkDO::getValidDate,null);
        //    requestParam.getValidDateType(), ValidDateTypeEnum.PERNAMENT.getType()), // 条件
        //    ShortLinkDO::getValidDate, // 要更新的字段
        //     null // 要设置的值
        baseMapper.update(shortLinkDO,updateWrapper);
    }

}
