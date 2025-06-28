package com.nageoffer.shortlink.project.mq.consumer;

import com.nageoffer.shortlink.project.common.convention.exception.ServiceException;
import com.nageoffer.shortlink.project.dto.biz.ShortLinkStatisticRecordDTO;
import com.nageoffer.shortlink.project.mq.idempotent.MessageQueueIdempotentHandler;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;

import static com.nageoffer.shortlink.project.common.constant.RedisKeyConstant.DELAY_QUEUE_STATISTIC_KEY;

/**
 * 延迟记录短链接统计组件
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DelayShortLinkStatisticConsumer implements InitializingBean {

    private final RedissonClient redissonClient;
    private final ShortLinkService shortLinkService;
    private final MessageQueueIdempotentHandler messageQueueIdempotentHandler;

    public void onMessage() {
        //这个消费者是在应用启动时会由 Spring 自动调用执行，不需要显式调用会，启动后会不断轮询延迟队列，取消息执行
        Executors.newSingleThreadExecutor(
                        runnable -> {
                            Thread thread = new Thread(runnable);
                            thread.setName("delay_short-link_statistic_consumer");
                            thread.setDaemon(Boolean.TRUE);
                            return thread;
                        })      //lambda表达式，创建一个线程
                .execute(() -> {
                    RBlockingDeque<ShortLinkStatisticRecordDTO> blockingDeque = redissonClient.getBlockingDeque(DELAY_QUEUE_STATISTIC_KEY);
                    //获取一个分布式阻塞双端队列，DELAY_QUEUE_STATISTIC_KEY 是这个队列在 Redis 中的键名。
                    RDelayedQueue<ShortLinkStatisticRecordDTO> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
                    //delayedQueue里的元素放进blockingDeque里
                    for (; ; ) {
                        //for (; ; ): 这是一个无限循环，表示消费者线程会持续运行，不断地尝试从队列中获取元素。
                        try {
                            ShortLinkStatisticRecordDTO statisticRecord = delayedQueue.poll();
                            //delayedQueue.poll(): 尝试从延迟队列中取出队首元素。
                            //如果队列中有已到期的元素，它会立即返回该元素。
                            //如果队列为空或者队列中所有元素都未到期，它会立即返回 null。
                            if (statisticRecord != null) {
                                if (!messageQueueIdempotentHandler.isMessageProcessed(statisticRecord.getKeys())) {
                                    // 判断当前的这个消息流程是否执行完成
                                    if (messageQueueIdempotentHandler.isAccomplish(statisticRecord.getKeys())) {
                                        return;
                                    }
                                    throw new ServiceException("消息未完成流程，需要消息队列重试");
                                }
                                try {
                                    shortLinkService.shortLinkStatistic(null, null, statisticRecord);
                                    //这里是有漏洞的，在 ShortLinkStatisticSaveConsumer 定义的消费逻辑里，如果fullShortUrl 为空，消费者不会消费信息，而是直接删除该信息
                                } catch (Throwable ex) {
                                    messageQueueIdempotentHandler.delMessageProcessed(statisticRecord.getKeys());
                                    log.error("延迟记录短链接监控消费异常", ex);
                                }
                                messageQueueIdempotentHandler.setAccomplish(statisticRecord.getKeys());
                                continue;  //后面的不执行了，跳到for循环的下一次迭代
                            }
                            LockSupport.parkUntil(500); //用于线程阻塞，线程会在这里暂停 500 毫秒。
                            //避免空转 (busy-waiting) 的策略，防止消费者线程在没有数据时持续高CPU占用
                        } catch (Throwable ignored) {
                        }
                        //try { ... } catch (Throwable ignored) { }表示异常被捕获但不进行任何处理，用于防止单个记录处理失败导致整个消费者停止
                    }
                });     // .execute(() -> {})是向该单线程提交一个任务
    }
    //这个方法的整体作用：
    //实现了一个基于 Redis 延迟队列的异步短链接统计消费者。
    //异步处理： 统计数据的处理被放在一个独立的后台线程中，不会阻塞主业务流程。
    //延迟消费： 某些统计数据可能需要延迟处理（例如，为了聚合一段时间内的点击数据，或者避免即时处理的压力）。通过将数据放入 RDelayedQueue 并指定延迟，可以控制其何时变为可消费。
    //持久化队列： Redisson 的分布式队列是基于 Redis 的，这意味着队列中的数据是持久化的。即使应用重启，未消费的延迟数据也不会丢失。
    //单线程消费： newSingleThreadExecutor 确保了统计记录是顺序地从队列中取出并处理的，这对于需要保证顺序性的统计场景很有用，同时也简化了并发控制。

    @Override
    public void afterPropertiesSet() throws Exception {
        onMessage();
    }
}
