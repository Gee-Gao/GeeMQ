package com.gee.mq.consumer;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.gee.mq.bean.QueueMQ;
import com.gee.mq.bean.Result;
import com.gee.mq.manage.MQManage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/consumer/queue")
public class QueueConsumer implements MQConsumer {
    private final MQManage mqManage;
    public static final String CONSUMER_QUEUE_PREFIX = "CONSUMER-QUEUE:";

    // 生成一个消费者
    @GetMapping
    public Result receiveMsg(String queueName, @RequestParam(required = false) Boolean realTime) {
        if (StrUtil.isEmpty(queueName)) {
            throw new RuntimeException("队列名不能为空");
        }

        Thread thread = consumerGenerate(queueName, realTime);

        while (isRepeatConsumerName(thread.getName())) {
            thread.setName(CONSUMER_QUEUE_PREFIX + queueName + ":" + UUID.fastUUID());
        }

        try {
            thread.start();
            log.info("消费者:{},启动", thread.getName());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("消费者:{},启动失败", thread.getName());
            return Result.error("消费者:" + thread.getName() + ",启动失败");
        }
        return Result.ok();
    }

    // 生成一个指定名字的消费者
    @GetMapping("/{consumerName}")
    public Result receiveMsg(@PathVariable("consumerName") String consumerName, String queueName, @RequestParam(required = false) Boolean realTime) {
        consumerCountCheck(CONSUMER_QUEUE_PREFIX, consumerName);

        if (StrUtil.isEmpty(queueName)) {
            throw new RuntimeException("队列名不能为空");
        }

        try {
            Thread thread = consumerGenerateWithConsumerName(consumerName, queueName, realTime);
            log.info("消费者:{},启动", consumerName);
            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("消费者:{},启动失败", consumerName);
            return Result.error("消费者:" + consumerName + ",启动失败");
        }
        return Result.ok();
    }

    // 获取当前所有消费者线程名字
    @GetMapping("/getConsumerName")
    public Result getConsumerName() {
        return Result.ok(consumerThreadName());
    }

    // 停止消费者
    @GetMapping("/stop/{consumerName}")
    public Result stopQueueConsumer(@PathVariable String consumerName) {
        stopConsumer(consumerName, CONSUMER_QUEUE_PREFIX);
        return Result.ok();
    }


    // 生成指定名字消费者线程
    private Thread consumerGenerateWithConsumerName(String consumerName, String queueName, Boolean realTime) {
        Thread thread = consumerGenerate(queueName, realTime);
        thread.setName(CONSUMER_QUEUE_PREFIX + queueName + ":" + consumerName);
        return thread;
    }

    // 获取消费者名称
    public List<String> consumerThreadName() {
        return consumerNameByPrefix(CONSUMER_QUEUE_PREFIX);
    }

    // 生成消费者线程
    public Thread consumerGenerate(String queueName, Boolean isRealTime) {
        if (isRealTime == null) {
            isRealTime = false;
        }

        Boolean finalIsRealTime = isRealTime;

        return new Thread(() -> {
            ConcurrentHashMap<String, QueueMQ> mqHashMap = mqManage.getQueueMqHashMap();
            while (!Thread.currentThread().isInterrupted()) {
                QueueMQ queueMq = mqHashMap.get(queueName);
                if (queueMq != null) {
                    ArrayBlockingQueue<String> queue = queueMq.getQueue();
                    if (queue != null) {
                        if (!queue.isEmpty()) {
                            String remove = queue.poll();
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
