package com.gee.mq.bean;

import lombok.Data;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;

@Data
public class TopicMQ {
    // 消费者名称
    private String consumerName;
    // 队列数据
    private ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(1024);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TopicMQ topicMQ = (TopicMQ) o;
        return Objects.equals(consumerName, topicMQ.consumerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(consumerName);
    }
}
