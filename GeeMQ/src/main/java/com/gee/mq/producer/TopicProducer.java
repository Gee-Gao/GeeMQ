package com.gee.mq.producer;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.gee.mq.bean.Result;
import com.gee.mq.bean.TopicMQ;
import com.gee.mq.consumer.MQConsumer;
import com.gee.mq.consumer.TopicConsumer;
import com.gee.mq.manage.MQManage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/producer/topic")
public class TopicProducer implements MQProducer {
    @Autowired
    @Qualifier("topicConsumer")
    private MQConsumer mqConsumer;

    private final MQManage mqManage;

    public static final String PRODUCER_TOPIC_PREFIX = "PRODUCER-TOPIC";

    // 获取待消费消息
    @GetMapping("pendingMsg")
    public Result pendingMsg(String topicName) {
        if (StrUtil.isEmpty(topicName)) {
            throw new RuntimeException("主题名不能为空");
        }

        Set<TopicMQ> set = mqManage.getTopicMqHashMap().get(topicName);
        if (set == null || set.size() == 0) {
            return Result.ok(new ArrayList<>(0));
        } else {
            ArrayList<Map<String, Object>> pendingMsg = new ArrayList<>(set.size());
            set.forEach(item -> {
                ArrayBlockingQueue<String> queue = item.getQueue();
                ArrayList<String> list = new ArrayList<>(queue.size());
                list.addAll(queue);
                Map<String, Object> map = new HashMap<>(2);
                map.put("consumerName", item.getConsumerName());
                map.put("pendingMessages", list);
                pendingMsg.add(map);
            });

            return Result.ok(pendingMsg);
        }
    }

    // 发送消息
    @GetMapping
    public void sendMsg(String topicName, String msg) {
        if (StrUtil.isEmpty(topicName)) {
            throw new RuntimeException("主题名不能为空");
        }

        if (StrUtil.isEmpty(msg)) {
            throw new RuntimeException("消息不能为空");
        }

        List<String> consumerNames = mqConsumer.consumerNameByPrefix(TopicConsumer.CONSUMER_TOPIC_PREFIX);

        ConcurrentHashMap<String, Set<TopicMQ>> topicMqHashMap = mqManage.getTopicMqHashMap();
        Set<TopicMQ> topicMQs = topicMqHashMap.get(topicName);
        if (topicMQs == null) {
            topicMQs = new HashSet<>(64);
            for (String consumerName : consumerNames) {
                TopicMQ topicMQ = new TopicMQ();
                topicMQ.setConsumerName(consumerName);
                topicMQ.getQueue().add(msg);
                topicMQs.add(topicMQ);
            }
        } else {
            for (String consumerName : consumerNames) {
                TopicMQ topicMQ = new TopicMQ();
                topicMQ.setConsumerName(consumerName);
                List<TopicMQ> collect = topicMQs.stream().filter(item -> item.equals(topicMQ)).collect(Collectors.toList());
                if (!collect.isEmpty()) {
                    collect.get(0).getQueue().add(msg);
                } else {
                    topicMQ.getQueue().add(msg);
                    topicMQs.add(topicMQ);
                }
            }
        }

        topicMqHashMap.put(topicName, topicMQs);
    }

    // 延时发送消息
    @GetMapping("/delay")
    public Result sendMsgDelay(String topicName, String msg, Long delay) {
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
            sendMsg(topicName, msg);
        }, PRODUCER_TOPIC_PREFIX + topicName + ":" + UUID.fastUUID()).start();

        return Result.ok();
    }

    // 循环发送消息
    @GetMapping("/loop")
    public Result sendMsgLoop(String topicName, String msg, Long loop, @RequestParam(required = false) Boolean firstWait) {
        if (loop == null) {
            throw new RuntimeException("请指定时间间隔");
        }

        if (firstWait == null || !firstWait) {
            sendMsg(topicName, msg);
        }

        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(loop);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendMsg(topicName, msg);
            }
        }, PRODUCER_TOPIC_PREFIX + topicName + ":" + UUID.fastUUID()).start();

        return Result.ok();
    }
}
