package com.gee.mq.consumer;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.gee.mq.bean.MQ;
import com.gee.mq.manage.MQManage;
import com.sun.jmx.remote.internal.ArrayQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/consumer/queue")
public class QueueConsumer implements MQConsumer {
    private final MQManage mqManage;
    public static final String CONSUMER_QUEUE_PREFIX = "CONSUMER-QUEUE:";

    // 生成一个消费者
    @GetMapping
    public void receiveMsg(String queueName, @RequestParam(required = false) Boolean realTime) {

        if (StrUtil.isEmpty(queueName)) {
            throw new RuntimeException("队列名不能为空");
        }

        Thread thread = consumerGenerate(queueName, realTime);

        while (isRepeatConsumerName(thread.getName())) {
            thread.setName(CONSUMER_QUEUE_PREFIX + queueName + ":" + UUID.fastUUID());
        }

        thread.start();
        log.info("消费者:{},已启动", thread.getName());
    }

    // 生成一个指定名字的消费者
    @GetMapping("/{consumerName}")
    public void receiveMsg(@PathVariable("consumerName") String consumerName, String queueName, @RequestParam(required = false) Boolean realTime) {
        long count = consumerThreads(CONSUMER_QUEUE_PREFIX).stream().filter(item -> item.getName().equals(CONSUMER_QUEUE_PREFIX + consumerName)).count();

        if (count > 0) {
            throw new RuntimeException("消费者已存在");
        }

        if (StrUtil.isEmpty(queueName)) {
            throw new RuntimeException("队列名不能为空");
        }

        Thread thread = consumerGenerateWithConsumerName(consumerName, queueName, realTime);
        thread.start();
        log.info("消费者:{},已启动", thread.getName());
    }

    // 获取当前所有消费者线程名字
    @GetMapping("/getThreadName")
    public List<String> consumerThreadName() {
        return consumerThreads(CONSUMER_QUEUE_PREFIX).stream().map(Thread::getName).collect(Collectors.toList());
    }

    // 停止消费者
    @GetMapping("/stop/{consumerName}")
    public void stopQueueConsumer(@PathVariable String consumerName) {
        stopConsumer(consumerName, CONSUMER_QUEUE_PREFIX);
    }

    // 生成指定名字消费者线程
    public Thread consumerGenerateWithConsumerName(String consumerName, String queueName, Boolean realTime) {
        Thread thread = consumerGenerate(queueName, realTime);
        thread.setName(CONSUMER_QUEUE_PREFIX + consumerName);
        return thread;
    }

    // 生成消费者线程
    private Thread consumerGenerate(String queueName, Boolean isRealTime) {
        if (isRealTime == null) {
            isRealTime = false;
        }

        Boolean finalIsRealTime = isRealTime;

        return new Thread(() -> {
            ConcurrentHashMap<String, MQ> mqHashMap = mqManage.getMqHashMap();
            while (!Thread.currentThread().isInterrupted()) {
                MQ mq = mqHashMap.get(queueName);
                if (mq != null) {
                    ArrayQueue<String> queue = mq.getQueue();
                    if (queue != null) {
                        if (!queue.isEmpty()) {
                            Object remove = queue.remove(0);
                            System.out.println(Thread.currentThread().getName() + "消费消息" + remove);
                        }
                    }
                }

                if (!finalIsRealTime) {
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        log.info("消费者:{},已停止", Thread.currentThread().getName());
                        break;
                    }
                }
            }
        }, CONSUMER_QUEUE_PREFIX + queueName + ":" + UUID.fastUUID());
    }

    // 判断是否消费者名字是否重复
    private boolean isRepeatConsumerName(String threadName) {
        return consumerThreadName().contains(threadName);
    }
}
