package com.gee.mq.bean;

import lombok.Data;

import java.util.concurrent.ArrayBlockingQueue;

@Data
public class QueueMQ {
    // 队列数据
    private ArrayBlockingQueue<String> queue=new ArrayBlockingQueue<>(1024);
}
