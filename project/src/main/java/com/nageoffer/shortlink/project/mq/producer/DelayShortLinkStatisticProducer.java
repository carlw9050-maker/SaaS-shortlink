package com.nageoffer.shortlink.project.mq.producer;

import com.nageoffer.shortlink.project.dto.biz.ShortLinkStatisticRecordDTO;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.nageoffer.shortlink.project.common.constant.RedisKeyConstant.DELAY_QUEUE_STATISTIC_KEY;

/**
 * 延迟消费短链接统计发送者
 */
@Component
@RequiredArgsConstructor
public class DelayShortLinkStatisticProducer {

    private final RedissonClient redissonClient;

    /**
     * 发送延迟消费短链接统计
     *
     * @param statisticRecord 短链接统计实体参数
     */
    public void send(ShortLinkStatisticRecordDTO statisticRecord) {
        RBlockingDeque<ShortLinkStatisticRecordDTO> blockingDeque = redissonClient.getBlockingDeque(DELAY_QUEUE_STATISTIC_KEY);
        RDelayedQueue<ShortLinkStatisticRecordDTO> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
        delayedQueue.offer(statisticRecord, 5, TimeUnit.SECONDS);
    }
}
