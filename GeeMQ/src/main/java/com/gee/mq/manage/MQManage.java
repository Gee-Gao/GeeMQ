package com.gee.mq.manage;

import com.gee.mq.bean.MQ;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
@Data
public class MQManage {
    private volatile ConcurrentHashMap<String, MQ> mqHashMap = new ConcurrentHashMap<>(1024);
}
