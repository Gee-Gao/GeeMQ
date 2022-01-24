package com.gee.mq.producer;

import com.gee.mq.bean.QueueMQ;
import com.gee.mq.manage.MQManage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("producer/queue")
public class QueueProducer implements MQProducer {
    private final MQManage mqManage;

    // 发送消息
    @GetMapping
    public void sendMsg(String queueName, String msg) {
        ConcurrentHashMap<String, QueueMQ> mqHashMap = mqManage.getMqHashMap();
        QueueMQ queueMq = mqHashMap.get(queueName);
        if (queueMq == null) {
            queueMq = new QueueMQ();
            queueMq.getQueue().add(msg);
            mqHashMap.put(queueName, queueMq);
        } else {
            queueMq.getQueue().add(msg);
        }
    }

    // 延时发送消息
    @GetMapping("/delay")
    public void sendMsgDelay(String queueName, String msg, Long delay) {
        if (delay == null) {
            delay = 0L;
        }
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sendMsg(queueName, msg);
    }

    // 循环发送消息
    @GetMapping("/loop")
    public void sendMsgLoop(String queueName, String msg, Long loop, @RequestParam(required = false) Boolean firstWait) {
        if (loop == null) {
            throw new RuntimeException("请指定时间间隔");
        }

        if (firstWait == null || !firstWait) {
            sendMsg(queueName, msg);
        }

        while (true) {
            try {
                Thread.sleep(loop);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sendMsg(queueName, msg);
        }

    }
}
