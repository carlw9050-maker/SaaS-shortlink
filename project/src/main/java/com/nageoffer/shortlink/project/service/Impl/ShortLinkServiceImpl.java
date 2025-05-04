package com.nageoffer.shortlink.project.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
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
import com.nageoffer.shortlink.project.dao.entity.ShortLinkGoToDO;
import com.nageoffer.shortlink.project.dao.mapper.ShortLinkGoToMapper;
import com.nageoffer.shortlink.project.dao.mapper.ShortLinkMapper;
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCountQueryRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageResDTO;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import com.nageoffer.shortlink.project.toolkit.HashUtil;
import com.nageoffer.shortlink.project.toolkit.LinkUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.nageoffer.shortlink.project.common.constant.RedisKeyConstant.*;

/**
 * 短链接接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;
    //该字段与方法名称一致，是一种约定做法，这样spring会自动将由@Bean注解的方法创建的Bean注入到该字段中
    private final ShortLinkGoToMapper shortLinkGoToMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;

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
        ShortLinkGoToDO linkGoToDO = ShortLinkGoToDO.builder()
                .fullShortUrl(fullShortUrl)
                .gid(requestParam.getGid())
                .build();
        try {
            baseMapper.insert(shortLinkDO);
            shortLinkGoToMapper.insert(linkGoToDO);
        }catch (DuplicateKeyException ex){
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getShortUri, fullShortUrl);
            ShortLinkDO hashShortLink = baseMapper.selectOne(queryWrapper);
            if(hashShortLink != null){
                log.warn("短链接：{} 重复入库",fullShortUrl);
                throw new ServiceException("短链接生成重复");
            }
        }
        stringRedisTemplate.opsForValue().set(
                String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                requestParam.getOriginUrl(),
                LinkUtil.getLinkCacheValidDate(requestParam.getValidDate()),TimeUnit.MILLISECONDS
        );
        //缓存预热
        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl("http://"+shortLinkDO.getFullShortUrl())
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

    @SneakyThrows
    @Override
    public void restoreUrl(String shortUri, ServletRequest request, ServletResponse response){
        String serverName = request.getServerName();
        String fullShortUrl = serverName + "/" + shortUri;
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY,fullShortUrl));
        //originalLink是字符串格式的key
        if(StrUtil.isNotBlank(originalLink)){
            ((HttpServletResponse) response).sendRedirect(originalLink);
            return;
        }
        //先查缓存里的键是否存在，存在则直接跳转，不存在则执行后续逻辑。
        boolean contains = shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl);
        if(!contains){
            return;
        }
        //使用布隆过滤器判断短链接是否存在，如果不存在则直接返回（防止缓存穿透）
        String gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY,fullShortUrl));
        if(StrUtil.isNotBlank(gotoIsNullShortLink)){
            return;
        }
        //检查是否存在标记为"空"的缓存（防止反复查询不存在的短链接
        //假设有人反复请求一个不存在的短链接 example.com/invalid123：
            // 第一次请求：查缓存 → 无；
            // 布隆过滤器 → 可能返回存在；布隆过滤器的局限性：虽然布隆过滤器可以快速判断"数据肯定不存在"，但它有假阳性率（可能误判存在）、不支持删除操作（一旦加入就一直存在）的缺陷
            // 查数据库 → 确认不存在；
            // 设置空值缓存 GOTO_IS_NULL_SHORT_LINK_KEY:example.com/invalid123 = "-"
        //后续请求：
            // 查缓存 → 无
            // 布隆过滤器 → 可能返回存在；
            //检查空值缓存 → 发现存在标记
            // 直接返回，不再继续查询
        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY,fullShortUrl));
        lock.lock();
        //创建一个分布式锁对象lock (RLock)，String.format()构造锁的唯一标识键
        // lock.lock():获取分布式锁（阻塞式），如果锁已被其他线程/进程持有，当前线程会阻塞等待,从而避免了缓存击穿
        try {
            originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isNotBlank(originalLink)) {
                ((HttpServletResponse) response).sendRedirect(originalLink);
                return;
            }
            //双重检查缓存：在锁内再次检查缓存，防止其他线程已经完成了缓存写入。
            LambdaQueryWrapper<ShortLinkGoToDO> linkGoToqueryWrapper = Wrappers.lambdaQuery(ShortLinkGoToDO.class)
                    .eq(ShortLinkGoToDO::getFullShortUrl, fullShortUrl);
            ShortLinkGoToDO shortLinkGoToDO = shortLinkGoToMapper.selectOne(linkGoToqueryWrapper);
            if (shortLinkGoToDO == null) {
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-",30, TimeUnit.MINUTES);
                return;
            }
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, shortLinkGoToDO.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0);
            ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
            if (shortLinkDO != null) {
                stringRedisTemplate.opsForValue().set(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl), shortLinkDO.getOriginUrl());
                //找到原始链接，则将key:原始链接 键值对存入缓存
                ((HttpServletResponse) response).sendRedirect(shortLinkDO.getOriginUrl());
                //作用是执行http重定向，将用户的请求重定向到短链接对应的原始链接上
            }
        }finally {
            lock.unlock();
           //确保锁一定被释放，不会死锁
        }
    }
    //代码能"先取后存"：这是典型的 缓存穿透防护设计，先尝试从 Redis 读取，如果缓存命中（短链接已存在），直接返回；如果未命中（返回 null），继续后续逻辑；
    //从数据库读取并回填缓存：查询数据库获取原始链接，将结果存入 Redis（供后续请求快速访问），这种设计避免了缓存穿透（大量请求直接打到数据库）

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
