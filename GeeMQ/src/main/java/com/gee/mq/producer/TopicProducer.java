package com.gee.mq.producer;

import cn.hutool.core.util.StrUtil;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    @GetMapping
    public void sendMsg(String topicName, String msg) {
        if (StrUtil.isEmpty(topicName)) {
            throw new RuntimeException("主题名不能为空");
        }

        if (StrUtil.isEmpty(msg)) {
            throw new RuntimeException("消息不能为空");
        }

        List<String> consumerNames = mqConsumer.consumerNameByPrefix(TopicConsumer.CONSUMER_TOPIC_PREFIX);
        System.out.println(consumerNames);
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
}
