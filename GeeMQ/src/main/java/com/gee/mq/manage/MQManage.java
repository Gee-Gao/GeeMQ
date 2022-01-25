package com.gee.mq.manage;

import com.gee.mq.bean.QueueMQ;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
@Data
public class MQManage {
    private volatile ConcurrentHashMap<String, QueueMQ> queueMqHashMap = new ConcurrentHashMap<>(1024);
}
