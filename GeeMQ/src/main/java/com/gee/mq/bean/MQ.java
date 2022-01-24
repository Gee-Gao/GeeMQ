package com.gee.mq.bean;

import com.sun.jmx.remote.internal.ArrayQueue;
import lombok.Data;

@Data
public class MQ{
    // 队列名
    private String queueName;
    // 队列数据
    private ArrayQueue<String> queue=new ArrayQueue<>(1024);
}
