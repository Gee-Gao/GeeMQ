package com.gee.mq.producer;

import cn.hutool.core.util.StrUtil;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface MQProducer {

    // 获取当前前缀所有生产者线程
    default List<Thread> producerThreads(String prefix) {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        int total = Thread.activeCount();

        Thread[] threads = new Thread[total];
        threadGroup.enumerate(threads);

        List<Thread> producerList = new ArrayList<>();

        for (Thread thread : threads) {
            if (StrUtil.startWith(thread.getName(), prefix)) {
                producerList.add(thread);
            }
        }
        return producerList;
    }


    // 获取指定前缀所有生产者名称
    default List<String> producerNameByPrefix(String prefix) {
        return producerThreads(prefix).stream().map(Thread::getName).collect(Collectors.toList());
    }

    // 停止指定前缀生产者
    default void stopProducer(@PathVariable String producerName, String prefix) {
        String[] producerNames = producerName.split(",");
        List<Thread> allProducer = producerThreads(prefix);
        for (String waitStopProducerName : producerNames) {
            for (Thread producer : allProducer) {
                if (waitStopProducerName.equals(producer.getName()) ||
                        (prefix + waitStopProducerName).equals(producer.getName())) {
                    producer.interrupt();
                }
            }
        }
    }
}
