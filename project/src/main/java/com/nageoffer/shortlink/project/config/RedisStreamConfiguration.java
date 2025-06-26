package com.nageoffer.shortlink.project.config;

import com.nageoffer.shortlink.project.mq.consumer.ShortLinkStatisticSaveConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Redis Stream 消息队列配置
 */
@Configuration
@RequiredArgsConstructor
public class RedisStreamConfiguration {

    private final RedisConnectionFactory redisConnectionFactory;
    //Redis 的连接工厂（用于构造 Stream 容器）。
    private final ShortLinkStatisticSaveConsumer shortLinkStatisticSaveConsumer;

    @Value("${spring.data.redis.channel-topic.short-link-statistic}")
    private String topic;
    @Value("${spring.data.redis.channel-topic.short-link-statistic-group}")
    private String group;

    @Bean
    public ExecutorService asyncStreamConsumer() {
        //异步线程池配置
        AtomicInteger index = new AtomicInteger();
        int processors = Runtime.getRuntime().availableProcessors();
        //创建线程池，线程命名使用自增索引。
        //processors 为可用 CPU 核数，便于控制线程池大小。
        return new ThreadPoolExecutor(processors,
                processors + processors >> 1,   //即processors + processors / 2
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                runnable -> {
                    Thread thread = new Thread(runnable);
                    thread.setName("stream_consumer_short-link_statistic_" + index.incrementAndGet());
                    thread.setDaemon(true);
                    //每个线程设置为守护线程，避免阻止 JVM 退出。
                    return thread;
                }
                //ThreadFactory函数式接口的Lambda表达式
        );
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    //initMethod="start"：启动时自动调用 start()；
    //destroyMethod="stop"：销毁时调用 stop()。
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer(ExecutorService asyncStreamConsumer) {
        //这是一个 Spring Bean 的工厂方法（在上层用 @Bean 注解），返回一个 Redis Stream 的消息监听容器。
        //参数 asyncStreamConsumer 是上面配置的线程池，用于异步执行消费逻辑。
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        //使用 builder 模式构造 StreamMessageListenerContainerOptions 对象,这是用于配置监听器行为的选项对象。
                        .batchSize(10)
                        // 一次最多获取多少条消息，这里的消息是从Redis的Stream类型的消息队列数据结构中拉取，而其中的消息又是生产者传来的
                        .executor(asyncStreamConsumer)
                        // 使用自定义的线程池来执行拉取与消费任务。
                        .pollTimeout(Duration.ofSeconds(3))
                        // 如果没有拉取到消息，需要阻塞的时间。不能大于 ${spring.data.redis.timeout}，否则连接会超时断开
                        //生产-消费背后的逻辑：消费线程发起一次 Redis 的XREADGROUP 请求，如果接收到新消息，则立即返回，然后再发出下一轮请求；
                        //如果没有收到新消息，则等待3秒，等待3秒，如果期间来了新消息，则立刻唤醒；如果一直没有新消息，则返回一个 null，再发起下一轮请求；
                        .build();
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer =
                StreamMessageListenerContainer.create(redisConnectionFactory, options);
        //使用Redis 连接工厂和配置选项创建一个 Redis Stream 消息监听容器。
        streamMessageListenerContainer.receiveAutoAck(Consumer.from(group, "statistic-consumer"),   //创建一个 Redis Stream 消费者身份：
                //消费者组名：group（从配置中读取）；消费者名称：statistic-consumer
                StreamOffset.create(topic, ReadOffset.lastConsumed()),  //订阅指定的 Stream：
                //Stream 名称为 topic（从配置中读取）；消费位置是 lastConsumed()：从上次消费到的位置开始（避免重复处理）。
                shortLinkStatisticSaveConsumer
                //消息处理器（实现 StreamListener 接口），处理消费后的业务逻辑，比如统计入库。
        );
        return streamMessageListenerContainer;
        //返回该容器，由 Spring 管理生命周期（启动和关闭）。
    }
}