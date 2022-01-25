package com.gee.mq.manage;

import com.gee.mq.bean.QueueMQ;
import com.gee.mq.bean.TopicMQ;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Data
public class MQManage {
    private volatile ConcurrentHashMap<String, QueueMQ> queueMqHashMap = new ConcurrentHashMap<>(1024);

    private volatile ConcurrentHashMap<String, Set<TopicMQ>> topicMqHashMap = new ConcurrentHashMap<>(1024);
}
