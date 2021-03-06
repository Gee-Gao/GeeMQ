package com.gee.mq.producer;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.gee.mq.bean.QueueMQ;
import com.gee.mq.bean.Result;
import com.gee.mq.manage.MQManage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("producer/queue")
public class QueueProducer implements MQProducer {
    public static final String PRODUCER_QUEUE_LOOP_PREFIX = "PRODUCER-QUEUE-LOOP:";
    public static final String PRODUCER_QUEUE_DELAY_PREFIX = "PRODUCER-QUEUE-DELAY:";
    private final MQManage mqManage;

    // 获取待消费消息数量
    @GetMapping("pendingMsgCountByName")
    public Result pendingMsgCountByName(String queueName) {
        if (StrUtil.isEmpty(queueName)) {
            throw new RuntimeException("队列名不能为空");
        }

        QueueMQ queueMQ = mqManage.getQueueMqHashMap().get(queueName);
        if (queueMQ == null) {
            return Result.ok(0);
        } else {
            return Result.ok(queueMQ.getQueue().size());
        }
    }

    // 获取待消费消息
    @GetMapping("pendingMsg")
    public Result pendingMsg(String queueName) {
        if (StrUtil.isEmpty(queueName)) {
            throw new RuntimeException("队列名不能为空");
        }

        QueueMQ queueMQ = mqManage.getQueueMqHashMap().get(queueName);
        if (queueMQ == null) {
            return Result.ok(new ArrayList<>(0));
        } else {
            ArrayBlockingQueue<String> queue = queueMQ.getQueue();
            ArrayList<String> pendingMsg = new ArrayList<>(queue.size());
            pendingMsg.addAll(queue);
            return Result.ok(pendingMsg);
        }
    }

    // 发送消息
    @GetMapping
    public Result sendMsg(String queueName, String msg) {
        if (StrUtil.isEmpty(queueName)) {
            throw new RuntimeException("队列名不能为空");
        }

        if (StrUtil.isEmpty(msg)) {
            throw new RuntimeException("消息不能为空");
        }

        ConcurrentHashMap<String, QueueMQ> mqHashMap = mqManage.getQueueMqHashMap();
        QueueMQ queueMq = mqHashMap.get(queueName);
        if (queueMq == null) {
            queueMq = new QueueMQ();
            queueMq.getQueue().add(msg);
            mqHashMap.put(queueName, queueMq);
        } else {
            queueMq.getQueue().add(msg);
        }

        return Result.ok();
    }

    // 延时发送消息
    @GetMapping("/delay")
    public Result sendMsgDelay(String queueName, String msg, Long delay) {
        if (delay == null) {
            delay = 0L;
        }

        Long finalDelay = delay;

        new Thread(() -> {
            try {
                Thread.sleep(finalDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sendMsg(queueName, msg);
        }, PRODUCER_QUEUE_DELAY_PREFIX + queueName + ":" + UUID.fastUUID()).start();

        return Result.ok();
    }

    // 循环发送消息
    @GetMapping("/loop")
    public Result sendMsgLoop(String queueName, String msg, Long loop, @RequestParam(required = false) Boolean firstWait) {
        if (loop == null) {
            throw new RuntimeException("请指定时间间隔");
        }

        if (firstWait == null || !firstWait) {
            sendMsg(queueName, msg);
        }

        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(loop);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendMsg(queueName, msg);
            }
        }, PRODUCER_QUEUE_LOOP_PREFIX + queueName + ":" + UUID.fastUUID()).start();
        return Result.ok();
    }

    // 获取循环发送消息的生产者
    @GetMapping("/getLoopProducer")
    public Result getLoopProducer() {
        return Result.ok(producerNameByPrefix(PRODUCER_QUEUE_LOOP_PREFIX));
    }

    // 根据名称停止循环发送消息的生产者
    @GetMapping("/stopLoopProducerByName/{producerName}")
    public Result stopLoopProducerByName(@PathVariable String producerName) {
        stopProducer(producerName, PRODUCER_QUEUE_LOOP_PREFIX);
        return Result.ok();
    }
}
