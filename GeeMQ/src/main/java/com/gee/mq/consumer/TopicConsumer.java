package com.gee.mq.consumer;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.gee.mq.bean.TopicMQ;
import com.gee.mq.manage.MQManage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/consumer/topic")
public class TopicConsumer implements MQConsumer {
    private final MQManage mqManage;
    public static final String CONSUMER_TOPIC_PREFIX = "CONSUMER-TOPIC:";

    @GetMapping
    public void receiveMsg(String topicName, @RequestParam(required = false) Boolean realTime) {

        if (StrUtil.isEmpty(topicName)) {
            throw new RuntimeException("主题名不能为空");
        }

        Thread thread = consumerGenerate(topicName, realTime);

        try {
            log.info("消费者:{},启动", thread.getName());
            thread.start();

        } catch (Exception e) {
            e.printStackTrace();
            log.error("消费者:{},启动失败", thread.getName());
        }
    }

    // 获取当前所有消费者线程名字
    @GetMapping("/getConsumerName")
    public List<String> consumerThreadName() {
        return consumerNameByPrefix(CONSUMER_TOPIC_PREFIX);
    }

    private Thread consumerGenerate(String topicName, Boolean isRealTime) {
        if (isRealTime == null) {
            isRealTime = false;
        }

        Boolean finalIsRealTime = isRealTime;

        return new Thread(() -> {
            ConcurrentHashMap<String, Set<TopicMQ>> topicMqHashMap = mqManage.getTopicMqHashMap();

            while (!Thread.currentThread().isInterrupted()) {
                Set<TopicMQ> set = topicMqHashMap.get(topicName);
                if(set!=null){
                    TopicMQ topicMQ = new TopicMQ();
                    topicMQ.setConsumerName(Thread.currentThread().getName());
                    List<TopicMQ> collect = set.stream().filter(item -> item.equals(topicMQ)).collect(Collectors.toList());
                    if(!collect.isEmpty()){

                        ArrayBlockingQueue<String> queue = collect.get(0).getQueue();
                        if(!queue.isEmpty()){
                            String msg = queue.poll();
                            System.out.println(Thread.currentThread().getName() + "消费消息" + msg);
                        }
                    }
                }

                if (!finalIsRealTime) {
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        log.info("消费者:{},已停止", Thread.currentThread().getName());
                        break;
                    }
                }
            }
        }, CONSUMER_TOPIC_PREFIX + topicName + ":" + UUID.fastUUID());
    }
}
