package com.gee.mq.bean;

import lombok.Data;

import java.util.Set;

@Data
public class Group {
    private String groupName;

    private Set<String> consumers;
}
