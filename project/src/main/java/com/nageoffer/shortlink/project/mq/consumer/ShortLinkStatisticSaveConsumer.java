package com.nageoffer.shortlink.project.mq.consumer;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nageoffer.shortlink.project.common.convention.exception.ServiceException;
import com.nageoffer.shortlink.project.dao.entity.*;
import com.nageoffer.shortlink.project.dao.mapper.*;
import com.nageoffer.shortlink.project.dto.biz.ShortLinkStatisticRecordDTO;
import com.nageoffer.shortlink.project.mq.idempotent.MessageQueueIdempotentHandler;
import com.nageoffer.shortlink.project.mq.producer.DelayShortLinkStatisticProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.nageoffer.shortlink.project.common.constant.RedisKeyConstant.LOCK_GID_UPDATE_KEY;

/**
 * 短链接监控状态保存消息队列消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShortLinkStatisticSaveConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    private final ShortLinkMapper shortLinkMapper;
    private final ShortLinkGoToMapper shortLinkGoToMapper;
    private final RedissonClient redissonClient;
    private final LinkAccessStatisticMapper linkAccessStatisticMapper;
//    private final LinkLocaleStatisticMapper linkLocaleStatisticMapper;
    private final LinkOsStatisticMapper linkOsStatisticMapper;
    private final LinkBrowserStatisticMapper linkBrowserStatisticMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkDeviceStatisticMapper linkDeviceStatisticMapper;
    private final LinkNetworkStatisticMapper linkNetworkStatisticMapper;
    private final DelayShortLinkStatisticProducer delayShortLinkStatisticProducer;
    private final StringRedisTemplate stringRedisTemplate;
    private final MessageQueueIdempotentHandler messageQueueIdempotentHandler;

//    @Valfue("${short-link.stats.locale.amap-key}")
//    private String statsLocaleAmapKey;

    /**
     * 消费逻辑
     * @param message 短链接访问信息统计的请求
     */
    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        //属于隐式（不声明接口但方法签名一致（Spring 会自动识别））地实现 StreamListener<String, MapRecord<String, String, String>> 接口，如此才可以作为消息处理器被使用
        //MapRecord<String, String, String> 是 Redis Stream 消息的封装对象，从左到右分别是消息所属的 Stream 名称（即topic）
        //消息的唯一 ID（如1690000000000-0，由 Redis 自动生成，以及 消息的实际内容（即生产者发送的producerMap，键值对结构）
        String stream = message.getStream();
        RecordId id = message.getId();
        if (!messageQueueIdempotentHandler.isMessageProcessed(id.toString())) {
            // 判断当前的这个消息是否已被消费
            if (messageQueueIdempotentHandler.isAccomplish(id.toString())) {
                return;
            }
            throw new ServiceException("消息未完成流程，需要消息队列重试");
        }
        try {
            Map<String, String> producerMap = message.getValue();  //获取消息的实际内容
            String fullShortUrl = producerMap.get("fullShortUrl");
            if (StrUtil.isNotBlank(fullShortUrl)) {
                String gid = producerMap.get("gid");
                ShortLinkStatisticRecordDTO statisticRecord = JSON.parseObject(producerMap.get("statisticRecord"), ShortLinkStatisticRecordDTO.class);
                actualSaveShortLinkStatistic(fullShortUrl, gid, statisticRecord);
            }
            stringRedisTemplate.opsForStream().delete(Objects.requireNonNull(stream), id.getValue());
            //删除 Redis 中已经成功处理的消息。 stream 是 Redis 的一个 key，它对应的值是 Stream 类型的一整个数据结构；.requireNonNull(stream) 确保名称不为空，否则抛出错误
            //id是 Stream 内的每条消息的唯一标识符，消息的实际内容通常包含一个或多个键值对。
        } catch (Throwable ex) {
            // 某某某情况宕机了
            messageQueueIdempotentHandler.delMessageProcessed(id.toString());
            log.error("记录短链接监控消费异常", ex);
        }
        messageQueueIdempotentHandler.setAccomplish(id.toString());  //删除幂等标识
        //总结，上述代码的逻辑就是：
        // 消息是否消费过？
            //是 那么直接返回，不执行后面逻辑；
            //否 → 去消费 → 删除 Stream 中已消费过的消息
                // 如果出异常，则删除幂等标识，便于下次重新消费。幂等标识自然过期失效的时长是 2 min。
            //标记消费完成（设置幂等标识为1）
    }

    /**
     * 短链接的访问信息的统计逻辑
     * 无须加入事务注解，因为 MySQL 在 InoDB 存储引擎下，将单个 INSERT/UPDATE/DELETE 视为一个独立的事务提交，那么由于事务饿 ACID 特性，在这里多个线程同时
     * 获取读锁，同时执行 INSERT 语句也不会出现数据不一致的问题；
     * @param fullShortUrl
     * @param gid
     * @param statisticRecord
     */
    public void actualSaveShortLinkStatistic(String fullShortUrl, String gid, ShortLinkStatisticRecordDTO statisticRecord) {
        fullShortUrl = Optional.ofNullable(fullShortUrl).orElse(statisticRecord.getFullShortUrl());
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(String.format(LOCK_GID_UPDATE_KEY, fullShortUrl));
        RLock rLock = readWriteLock.readLock();
        if (!rLock.tryLock()) {
            delayShortLinkStatisticProducer.send(statisticRecord);
            return;
        }   //没有获取到读锁，那么将该请求放至延迟队列；获取到了则执行后续逻辑
        try {
            if (StrUtil.isBlank(gid)) {
                LambdaQueryWrapper<ShortLinkGoToDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkGoToDO.class)
                        .eq(ShortLinkGoToDO::getFullShortUrl, fullShortUrl);
                ShortLinkGoToDO shortLinkGoToDO = shortLinkGoToMapper.selectOne(queryWrapper);
                gid = shortLinkGoToDO.getGid();
            }
            //如果该方法的输入参数里gid为空，那么查询该短链接对应的gid
            int hour = DateUtil.hour(new Date(), true);
            Week week = DateUtil.dayOfWeekEnum(new Date());
            //在 Hutool 的 DateUtil.dayOfWeekEnum() 方法中，返回的 Week 枚举代表的是星期几（Monday-Sunday）
            int weekValue = week.getIso8601Value();
            //而 getIso8601Value() 方法返回的是 ISO 8601 标准的星期序号，其中：星期一（Monday）= 1、星期二（Tuesday）= 2...
            LinkAccessStatisticDO linkAccessStatisticDO = LinkAccessStatisticDO.builder()
                    .pv(1)
                    .uv(statisticRecord.getUvFirstFlag() ? 1 : 0)
                    .uip(statisticRecord.getUipFirstFlag() ? 1 : 0)
                    .hour(hour)
                    .weekday(weekValue)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .build();
            linkAccessStatisticMapper.shortLinkStatistic(linkAccessStatisticDO);
            //更新t_linkAccessStatistic表中短链接的访问记录
            shortLinkMapper.incrementStats(gid, fullShortUrl, 1, statisticRecord.getUvFirstFlag() ? 1 : 0, statisticRecord.getUipFirstFlag() ? 1 : 0);
            //更新t_link里的total_的三个字段值
//        Map<String,Object> localeParamMap = new HashMap();
//        localeParamMap.put("key", statisticLocaleAmapKey);
//        localeParamMap.put("ip", remoteAddr);
//        String localeResultStr = HttpUtil.get(AMAP_REMOTE_URL,localeParamMap);
//        JSONObject localeResultObj = JSON.parseObject(localeResultStr);
//        String infoCode = localeResultObj.getString("infoCode");
//        if(StrUtil.isBlank(infoCode) && StrUtil.equals(infoCode,"1000")){
//            String province = localeResultObj.getString("province");
//            boolean unknownFlag = StrUtil.equals(province,"[]");
//            LinkLocaleStatisticDO linkLocaleStatisticDO = LinkLocaleStatisticDO.builder()
//                    .province(unknownFlag ? "未知" : province)
//                    .city(unknownFlag ? "未知" : localeResultObj.getString("city"))
//                    .adcode(unknownFlag ? "未知" : localeResultObj.getString("adcode"))
//                    .cnt(1)
//                    //cnt 字段表示特定短链接被来自特定地理位置（由省份、城市和行政区划代码定义）访问的次数。
//                    .fullShortUrl(fullShortUrl)
//                    .country("中国")
//                    .gid(gid)
//                    .date(new Date())
//                    .build();
//            linkLocaleStatisticMapper.shortLinkLocaleStatistic(linkLocaleStatisticDO);

            LinkOsStatisticDO linkOsStatisticDO = LinkOsStatisticDO.builder()
                    .os(statisticRecord.getOs())
                    .cnt(1)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .build();
            linkOsStatisticMapper.shortLinkOsStatistic(linkOsStatisticDO);

            LinkBrowserStatisticDO linkBrowserStatisticDO = LinkBrowserStatisticDO.builder()
                    .browser(statisticRecord.getBrowser())
                    .cnt(1)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .build();
            linkBrowserStatisticMapper.shortLinkBrowserStatistic(linkBrowserStatisticDO);

            LinkDeviceStatisticDO linkDeviceStatisticDO = LinkDeviceStatisticDO.builder()
                    .device(statisticRecord.getDevice())
                    .cnt(1)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .build();
            linkDeviceStatisticMapper.shortLinkDeviceStatistic(linkDeviceStatisticDO);

            LinkNetworkStatisticDO linkNetworkStatisticDO = LinkNetworkStatisticDO.builder()
                    .network(statisticRecord.getNetwork())
                    .cnt(1)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .build();
            linkNetworkStatisticMapper.shortLinkNetworkStatistic(linkNetworkStatisticDO);

            LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                    .user(statisticRecord.getUv())
                    .ip(statisticRecord.getRemoteAddr())
                    .browser(statisticRecord.getBrowser())
                    .fullShortUrl(fullShortUrl)
                    .os(statisticRecord.getOs())
                    .gid(gid)
                    .build();
            linkAccessLogsMapper.insert(linkAccessLogsDO);
        } catch (Throwable ex) {
            log.error("短链接访问异常", ex);
        } finally {
            rLock.unlock();
        }
    }
    //The read lock here is a defensive mechanism to prevent the statistics collection from running concurrently with a write lock
    // held by an operation that might fundamentally alter the short link's properties or existence. If such a write operation
    // is in progress, the statistics request is temporarily deferred to maintain data integrity and prevent potential errors
    // from trying to collect statistics on a short link that might be in an inconsistent state or no longer exist in its original group.
    //写锁的位置是在updateShortLink那儿
}
