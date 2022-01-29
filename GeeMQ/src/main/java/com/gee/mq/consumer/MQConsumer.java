package com.gee.mq.consumer;

import cn.hutool.core.util.StrUtil;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface MQConsumer {
    // 获取当前前缀所有消费者线程
    default List<Thread> consumerThreads(String prefix) {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        int total = Thread.activeCount();

        Thread[] threads = new Thread[total];
        threadGroup.enumerate(threads);

        List<Thread> consumerList = new ArrayList<>();

        for (Thread thread : threads) {
            if (StrUtil.startWith(thread.getName(), prefix)) {
                consumerList.add(thread);
            }
        }
        return consumerList;
    }

    // 获取指定前缀所有消费者名称
    default List<String> consumerNameByPrefix(String prefix) {
        return consumerThreads(prefix).stream().map(Thread::getName).collect(Collectors.toList());
    }

    // 停止指定前缀消费者
    default void stopConsumer(@PathVariable String consumerName, String prefix) {
        String[] consumerNames = consumerName.split(",");
        List<Thread> allConsumer = consumerThreads(prefix);
        for (String waitStopConsumerName : consumerNames) {
            for (Thread consumer : allConsumer) {
                if (waitStopConsumerName.equals(consumer.getName()) ||
                        (prefix + waitStopConsumerName).equals(consumer.getName())) {
                    consumer.interrupt();
                }
            }
        }
    }

    // 计算指定前缀消费者数量
    default long consumerCount(String prefix, String consumerName) {
        return consumerThreads(prefix).stream().filter(item -> item.getName().equals(prefix + consumerName)).count();
    }

    // 消费者重复检查
    default void consumerCountCheck(String prefix, String consumerName) {
        long count = consumerCount(prefix, consumerName);
        if (count > 0) {
            throw new RuntimeException(prefix + "类型消费者已存在");
        }
    }
}
