package com.gee.mq.manage;

import com.gee.mq.bean.Group;
import com.gee.mq.consumer.QueueConsumer;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Data
@Component
@RequiredArgsConstructor
@RequestMapping("group")
public class GroupManage {
    private volatile ConcurrentHashMap<String, Group> groupHashMap = new ConcurrentHashMap<>(1024);

    private final QueueConsumer queueConsumer;
    // 消费者分组
    @GetMapping("queue")
    public void groupConsumer(String groupName, Set<String> consumerNames) {
        List<String> consumerThreadNameList = queueConsumer.consumerThreadName();
        Group group = groupHashMap.get(groupName);
        if (group == null) {
            group=new Group();
            group.setGroupName(groupName);
            Set<String> collect = consumerNames.stream()
                    .filter(consumerThreadNameList::contains)
                    .collect(Collectors.toSet());
            group.setConsumers(collect);
            groupHashMap.put(groupName,group);
        }else {
            Set<String> consumers = group.getConsumers();
            Set<String> collect = consumerNames.stream()
                    .filter(consumerThreadNameList::contains)
                    .collect(Collectors.toSet());
            consumers.addAll(collect);
        }
    }
}
