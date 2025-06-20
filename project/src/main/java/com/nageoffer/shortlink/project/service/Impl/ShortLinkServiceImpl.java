package com.nageoffer.shortlink.project.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ArrayUtil;
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
import com.nageoffer.shortlink.project.dao.entity.*;
import com.nageoffer.shortlink.project.dao.mapper.*;
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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
    private final LinkAccessStatisticMapper linkAccessStatisticMapper;
    private final LinkLocaleStatisticMapper linkLocaleStatisticMapper;
    private final LinkOsStatisticMapper linkOsStatisticMapper;
    private final LinkBrowserStatisticMapper linkBrowserStatisticMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkDeviceStatisticMapper linkDeviceStatisticMapper;
    private final LinkNetworkStatisticMapper linkNetworkStatisticMapper;

//    @Value("${short-link.statistic.locale.amap-key}")
//    private String statisticLocaleAmapKey;
    //将配置文件application.yml中键为 short-link.statistic.locale.amap-key 的属性值注入到名为 statisticLocaleAmapKey的成员变量中。
    @Value("${short-link.domain.default}")
    private String createShortLinkDefaultDomain;  //在后端指定默认域名

    /**
     * 创建短链接
     */
    @Override
    public ShortLinkCreateRespDTO creatShortLink(ShortLinkCreateReqDTO requestParam) {
        String shortLinkSuffix = generateSuffix(requestParam);
        String fullShortUrl = StrBuilder.create(createShortLinkDefaultDomain)
                .append("/").append(shortLinkSuffix)
                .toString();
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(createShortLinkDefaultDomain)
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .createdType(requestParam.getCreatedType())
                .validDate(requestParam.getValidDate())
                .validDateType(requestParam.getValidDateType())
                .describe(requestParam.getDescribe())
                .shortUri(shortLinkSuffix)
                .enableStatus(0)
                .totalPv(0)
                .totalUv(0)
                .totalUip(0)
                .fullShortUrl(fullShortUrl)
                .favicon(getFavicon(requestParam.getOriginUrl()))
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
        String serverName = request.getServerName();  //获得域名
        String serverPort = Optional.of(request.getServerPort())
                //Optional是一个容器类，Optional.of(value)表示创建一个包含value（明确不为null）的实例
                .filter(each -> !Objects.equals(each, 80))
                //如果判断为false，this filter will cause Optional实例为空，会跳过后面的.map方法，直接执行.orElse(""),返回一个空字符串
                .map(String::valueOf)
                .map(each -> ":" + each)
                .orElse("");
        String fullShortUrl = serverName + serverPort + "/" + shortUri;
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY,fullShortUrl));
        //originalLink是字符串格式的key；
        //stringRedisTemplate is an instance of StringRedisTemplate，是与redis server交互的client
        //.opsForValue() returns a ValueOperations interface
        //.get() was used to retrieve（获取） the value associated with a given key from Redis.
        //String.format(...) was used for creating formatted strings,eg:String.format("user:%s:data", "123") would result in the key "user:123:data".
        if(StrUtil.isNotBlank(originalLink)){
            shortLinkStatistic(fullShortUrl,null,request,response);
            ((HttpServletResponse) response).sendRedirect(originalLink);
            return;
        }
        //先查缓存里的原始链接是否存在，存在则直接跳转，不存在则执行后续逻辑。
        boolean contains = shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl);
        if(!contains){
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }
        //判断布隆过滤器里的短链接是否存在，如果存在，执行后续逻辑；如果不存在，则说明数据库不存在该短链接，返回”页面不存在“（防止缓存穿透）
        String gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY,fullShortUrl));
        if(StrUtil.isNotBlank(gotoIsNullShortLink)){
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
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
                shortLinkStatistic(fullShortUrl,null,request,response);
                ((HttpServletResponse) response).sendRedirect(originalLink);
                return;
            }
            //双重检查缓存：在锁内再次检查缓存，防止其他线程已经完成了缓存写入。
            LambdaQueryWrapper<ShortLinkGoToDO> linkGoToqueryWrapper = Wrappers.lambdaQuery(ShortLinkGoToDO.class)
                    .eq(ShortLinkGoToDO::getFullShortUrl, fullShortUrl);
            ShortLinkGoToDO shortLinkGoToDO = shortLinkGoToMapper.selectOne(linkGoToqueryWrapper);
            if (shortLinkGoToDO == null) {
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-",30, TimeUnit.MINUTES);
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, shortLinkGoToDO.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0);
            ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
            if (shortLinkDO != null) {
                if(shortLinkDO.getValidDate() !=null && shortLinkDO.getValidDate().before(new Date())){
                    stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-",30, TimeUnit.MINUTES);
                    //过期短链接将其缓存内的标识设为空，即软删除
                    ((HttpServletResponse) response).sendRedirect("/page/notfound");
                    return;
                }
                stringRedisTemplate.opsForValue().set(
                        String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                        shortLinkDO.getOriginUrl(),
                        LinkUtil.getLinkCacheValidDate(shortLinkDO.getValidDate()),TimeUnit.MILLISECONDS
                );
                //找到原始链接，则将key:原始链接 键值对存入缓存
                shortLinkStatistic(fullShortUrl,shortLinkDO.getGid(),request,response);
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

    private void shortLinkStatistic(String fullShortUrl, String gid, ServletRequest request, ServletResponse response){

        AtomicBoolean uvFirstFlag = new AtomicBoolean();    //默认初始值是false
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        //((HttpServletRequest) request)是执行强制类型转换，request是HttpServletRequest 接口的一个实例。
        // HttpServletRequest 对象代表了客户端（例如，网页浏览器）发送过来的 HTTP 请求。
        //cookie是包含在客户端发送过来的http请求里的；如果客户端没有发送任何 Cookie，这个方法会返回 null
        try{
            AtomicReference<String> uv = new AtomicReference<>();
            Runnable addResponseCookieTask = () -> {
                uv.set(UUID.fastUUID().toString());
                //uv是一个不可改变的通用唯一标识
                Cookie uvCookie = new Cookie("uv", uv.get());
                //创建一个Cookie实例，名称是uv，值是上面的字符串
                uvCookie.setMaxAge(60 * 60 * 24 * 30);
                //设置uvCookie的最大生存时间，到期后浏览器不再存储这个Cookie
                uvCookie.setPath(StrUtil.sub(fullShortUrl,fullShortUrl.indexOf("/"),fullShortUrl.length()));
                //uvCookie.setPath()，浏览器只有在请求这个路径或其子路径下的资源，http请求才会携带这个Cookie，发送给服务器
                //StrUtil.sub()截取fullShortUrl中从第一个/到末尾的子字符串
                ((HttpServletResponse) response).addCookie(uvCookie);
                //将指定的Cookie加入到http响应的头部中，浏览器会存储这个Cookie，并且后续向相同服务器相同路径下的请求中会包含这个Cookie（未过期）
                uvFirstFlag.set(Boolean.TRUE);
                stringRedisTemplate.opsForSet().add("short-link:statistic:uv:" + fullShortUrl, uv.get());
            };
            if(ArrayUtil.isNotEmpty(cookies)){
                //如果 cookies 数组不为空，这行代码会创建一个基于该数组的 Stream，可以进行链式操作
                Arrays.stream(cookies)
                        .filter(each -> Objects.equals(each.getName(), "uv"))
                        //过滤操作，检查数组中每一个元素的名称是否与uv相同，只有相同的Cookie会被保留在Stream中
                        .findFirst()//查找Stream的第一个元素
                        .map(Cookie::getValue)
                        //将Optional<Cookie>中的Cookie对象转为其值
                        .ifPresentOrElse(each -> {
                            //uv.set(each);此代码是多余的
                            //初始化uv字段
                            Long added = stringRedisTemplate.opsForSet().add("short-link:statistic:uv:" + fullShortUrl,each);
                            //对redis的set集合执行add操作，"short-link:statistic:uv:" + fullShortUrl是set集合的key，each是从名为uv的Cookie中获取的值
                            //added是成功添加到set集合里的元素个数；如果set集合里已有相同元素，opsForSet().add()不会重复添加
                            uvFirstFlag.set(added != null && added > 0L);
                        },addResponseCookieTask);//如果each有值(即名为uv对应的值），则 执行前一个，若each无值，则执行该任务
                //.ifPresentOrElse(each -> { ... }, addResponseCookieTask)，接受两个参数;.ifPresentOrElse()是Optional类提供的方法
            }else{
                addResponseCookieTask.run();
            }
            //上述代码的逻辑是：检查http请求里有无任何Cookie，
                // 若有
                    // 则尝试找到指定Cookie，加入到redis的set里，若成功添加，则uvFirstFlag设置为True；
                    // 若没有找到指定Cookie，则创建一个，并加入到http响应中
                // 若没有，则创建一个Cookie，并加入到http响应中，后续http请求都会带上该Cookie
            String remoteAddr = LinkUtil.getActualIp(((HttpServletRequest) request));
            Long uipAdded = stringRedisTemplate.opsForSet().add("short-link:statistic:uip:" + fullShortUrl,remoteAddr);
            boolean uipAddedFlag = uipAdded != null && uipAdded > 0L;
            if(StrUtil.isBlank(gid)){
                LambdaQueryWrapper<ShortLinkGoToDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkGoToDO.class)
                        .eq(ShortLinkGoToDO::getFullShortUrl, fullShortUrl);
                ShortLinkGoToDO shortLinkGoToDO = shortLinkGoToMapper.selectOne(queryWrapper);
                gid = shortLinkGoToDO.getGid();
            }
            int hour = DateUtil.hour(new Date(),true);
            Week week = DateUtil.dayOfWeekEnum(new Date());
            //在 Hutool 的 DateUtil.dayOfWeekEnum() 方法中，返回的 Week 枚举代表的是星期几（Monday-Sunday）
            int weekValue = week.getIso8601Value();
            //而 getIso8601Value() 方法返回的是 ISO 8601 标准的星期序号，其中：星期一（Monday）= 1、星期二（Tuesday）= 2...
            LinkAccessStatisticDO linkAccessStatisticDO = LinkAccessStatisticDO.builder()
                    .pv(1)
                    .uv(uvFirstFlag.get() ? 1 : 0)
                    .uip(uipAddedFlag ? 1 : 0)
                    .hour(hour)
                    .weekday(weekValue)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .build();
            linkAccessStatisticMapper.shortLinkStatistic(linkAccessStatisticDO);

            //更新段短链接的总访问量
            baseMapper.incrementStats(gid, fullShortUrl, 1, uvFirstFlag.get() ? 1 : 0, uipAddedFlag ? 1 : 0);

//            Map<String,Object> localeParamMap = new HashMap();
//            localeParamMap.put("key", statisticLocaleAmapKey);
//            localeParamMap.put("ip", remoteAddr);
//            String localeResultStr = HttpUtil.get(AMAP_REMOTE_URL,localeParamMap);
//            JSONObject localeResultObj = JSON.parseObject(localeResultStr);
//            String infoCode = localeResultObj.getString("infoCode");
//            if(StrUtil.isBlank(infoCode) && StrUtil.equals(infoCode,"1000")){
//                String province = localeResultObj.getString("province");
//                boolean unknownFlag = StrUtil.equals(province,"[]");
//                LinkLocaleStatisticDO linkLocaleStatisticDO = LinkLocaleStatisticDO.builder()
//                        .province(unknownFlag ? "未知" : province)
//                        .city(unknownFlag ? "未知" : localeResultObj.getString("city"))
//                        .adcode(unknownFlag ? "未知" : localeResultObj.getString("adcode"))
//                        .cnt(1)
//                        //cnt 字段表示特定短链接被来自特定地理位置（由省份、城市和行政区划代码定义）访问的次数。
//                        .fullShortUrl(fullShortUrl)
//                        .country("中国")
//                        .gid(gid)
//                        .date(new Date())
//                        .build();
//                linkLocaleStatisticMapper.shortLinkLocaleStatistic(linkLocaleStatisticDO);
            String os = LinkUtil.getOs(((HttpServletRequest) request));
            LinkOsStatisticDO linkOsStatisticDO = LinkOsStatisticDO.builder()
                    .os(os)
                    .cnt(1)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .build();
            linkOsStatisticMapper.shortLinkOsStatistic(linkOsStatisticDO);
            String browser = LinkUtil.getBrowser(((HttpServletRequest) request));
            LinkBrowserStatisticDO linkBrowserStatisticDO = LinkBrowserStatisticDO.builder()
                    .browser(browser)
                    .cnt(1)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .build();
            linkBrowserStatisticMapper.shortLinkBrowserStatistic(linkBrowserStatisticDO);
            LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                    .user(uv.get())
                    .ip(remoteAddr)
                    .browser(browser)
                    .fullShortUrl(fullShortUrl)
                    .os(os)
                    .gid(gid)
                    .build();
            linkAccessLogsMapper.insert(linkAccessLogsDO);
            LinkDeviceStatisticDO linkDeviceStatisticDO = LinkDeviceStatisticDO.builder()
                    .device(LinkUtil.getDevice(((HttpServletRequest) request)))
                    .cnt(1)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .build();
            linkDeviceStatisticMapper.shortLinkDeviceStatistic(linkDeviceStatisticDO);
            LinkNetworkStatisticDO linkNetworkStatisticDO = LinkNetworkStatisticDO.builder()
                    .network(LinkUtil.getNetwork(((HttpServletRequest) request)))
                    .cnt(1)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .build();
            linkNetworkStatisticMapper.shortLinkNetworkStatistic(linkNetworkStatisticDO);
        } catch (Throwable ex) {
            log.error("短链接访问异常",ex);
        }
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
        baseMapper.update(shortLinkDO,updateWrapper);  //前者是各字段待更新的新值，后者是更新条件（数据库里满足条件的记录才会更新新值）
    }

    @SneakyThrows
    private String getFavicon(String url) {

        URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            Document document = Jsoup.connect(url).get();
            Element faviconlink = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
            if (faviconlink != null) {
                return faviconlink.attr("abs:href");
            }
        }
        return null;
    }

}
