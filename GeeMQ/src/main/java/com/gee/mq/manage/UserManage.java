package com.gee.mq.manage;

import com.gee.mq.bean.Result;
import com.gee.mq.bean.User;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Data

@RestController
@RequiredArgsConstructor
@RequestMapping("user")
public class UserManage {

    static List<User> allUsers;

    static {
        allUsers=new ArrayList<>();

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword("123456");
        allUsers.add(admin);

        User general = new User();
        general.setUsername("general");
        general.setPassword("123456");
        allUsers.add(general);

        User groupManage = new User();
        groupManage.setUsername("groupManage");
        groupManage.setPassword("123456");
        allUsers.add(groupManage);

        User consumerManage = new User();
        consumerManage.setUsername("consumerManage");
        consumerManage.setPassword("123456");
        allUsers.add(consumerManage);

        User producerManage = new User();
        producerManage.setUsername("producerManage");
        producerManage.setPassword("123456");
        allUsers.add(consumerManage);
    }

    @PostMapping("login")
    private Result login(User user) {
        if ("admin".equals(user.getUsername()) && "123456".equals(user.getPassword())) {
            return Result.ok();
        } else {
            throw new RuntimeException("账号或密码错误");
        }
    }
}
