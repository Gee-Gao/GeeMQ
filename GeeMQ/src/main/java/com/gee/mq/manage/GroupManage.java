package com.gee.mq.manage;

import com.gee.mq.bean.Group;
import com.gee.mq.bean.Result;
import com.gee.mq.consumer.QueueConsumer;
import com.gee.mq.consumer.TopicConsumer;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Data
@Component
@RestController
@RequiredArgsConstructor
@RequestMapping("group")
public class GroupManage {
    private volatile ConcurrentHashMap<String, Group> groupHashMap = new ConcurrentHashMap<>(1024);

    private final QueueConsumer queueConsumer;

    private final TopicConsumer topicConsumer;

    // 消费者分组
    @GetMapping
    public void groupConsumer(String groupName, Set<String> consumerNames) {
        List<String> queueConsumerNameList = queueConsumer.consumerThreadName();
        List<String> topicConsumerNameList = topicConsumer.consumerThreadName();

        List<String> consumerNameList = new ArrayList<>(queueConsumerNameList.size() + topicConsumerNameList.size());

        consumerNameList.addAll(queueConsumerNameList);
        consumerNameList.addAll(topicConsumerNameList);

        Group group = groupHashMap.get(groupName);
        if (group == null) {
            group = new Group();
            group.setGroupName(groupName);
            Set<String> collect = consumerNames.stream()
                    .filter(consumerNameList::contains)
                    .collect(Collectors.toSet());
            group.setConsumers(collect);
            groupHashMap.put(groupName, group);
        } else {
            Set<String> consumers = group.getConsumers();
            Set<String> collect = consumerNames.stream()
                    .filter(consumerNameList::contains)
                    .collect(Collectors.toSet());
            consumers.addAll(collect);
        }
    }

    // 获取分组
    @GetMapping("getAllGroup")
    public Result getAllGroup() {
        List<Group> groups = new ArrayList<>();
        groupHashMap.forEach((k, v) -> groups.add(v));
        return Result.ok(groups);
    }

    // 根据名称获取指定分组
    @GetMapping("getGroupByName/{groupName}")
    public Result getGroupByName(@PathVariable String groupName) {
        return Result.ok(groupHashMap.get(groupName));
    }

    // 根据组名停止消费者
    @GetMapping("stopGroup/{groupName}")
    public Result stopGroup(@PathVariable String groupName) {
        Group group = groupHashMap.get(groupName);
        if (group != null) {
            Set<String> consumers = group.getConsumers();

            List<Thread> queueThreads = queueConsumer.consumerThreads(QueueConsumer.CONSUMER_QUEUE_PREFIX);
            List<Thread> topicThreads = topicConsumer.consumerThreads(TopicConsumer.CONSUMER_TOPIC_PREFIX);
            List<Thread> allThread = new ArrayList<>(queueThreads.size() + topicThreads.size());

            allThread.addAll(queueThreads);
            allThread.addAll(topicThreads);

            consumers.forEach(waitStopConsumerName ->
                    allThread.forEach(consumer -> {
                        if (consumer.getName().equals(waitStopConsumerName)) {
                            consumer.interrupt();
                        }
                    })
            );
        }
        return Result.ok();
    }

    // 生成分组
    @GetMapping("generateGroup")
    public Result generateGroup(String groupName, String type, String queueOrTopicName, Integer count, Boolean realTime) {
        if (groupName == null) {
            throw new RuntimeException("分组名不能为空");
        }

        if (type == null) {
            throw new RuntimeException("类型不能为空");
        }

        if (queueOrTopicName == null) {
            throw new RuntimeException("队列名或主题名不能为空");
        }

        if (count <= 0) {
            throw new RuntimeException("消费者数量错误");
        }

        if (realTime == null) {
            realTime = false;
        }
        Set<String> consumerNames = new HashSet<>(count);

        if ("group".equals(type)) {
            for (int i = 0; i < count; i++) {
                Thread thread = queueConsumer.consumerGenerate(queueOrTopicName, realTime);
                consumerNames.add(thread.getName());
            }
        }

        if ("topic".equals(type)) {
            for (int i = 0; i < count; i++) {
                Thread thread = topicConsumer.consumerGenerate(queueOrTopicName, realTime);
                consumerNames.add(thread.getName());
            }
        }

        if (!consumerNames.isEmpty()) {
            groupConsumer(groupName,consumerNames);
        }
        return Result.ok();
    }
}
