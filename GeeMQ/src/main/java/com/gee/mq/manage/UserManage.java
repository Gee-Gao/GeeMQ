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
        allUsers = new ArrayList<>();

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

    @PostMapping("register")
    public Result changePassword(User user) {
        if (user.getUsername() == null) {
            throw new RuntimeException("账号不能为空");
        }

        if (user.getPassword() == null) {
            throw new RuntimeException("密码不能为空");
        }

        long count = allUsers.stream().filter(item -> item.equals(user)).count();

        if(count==0){
            throw new RuntimeException("账号或密码错误");
        }

        if(user.getNewPassword()==null){
            throw new RuntimeException("新密码不能为空");
        }

        allUsers.stream().filter(item -> item.equals(user)).forEach(item->item.setPassword(user.getPassword()));

        return Result.ok();
    }


    @PostMapping("login")
    public Result login(User user) {
        long count = allUsers.stream().filter(item -> item.equals(user)).count();
        if (count > 0) {
            return Result.ok();
        } else {
            throw new RuntimeException("账号或密码错误");
        }
    }

    @PostMapping("register")
    public Result register(User user) {
        if (user.getUsername() == null) {
            throw new RuntimeException("账号不能为空");
        }

        if (user.getPassword() == null) {
            throw new RuntimeException("密码不能为空");
        }

        long count = allUsers.stream().filter(item -> item.getUsername().equals(user.getUsername())).count();

        if (count > 0) {
            throw new RuntimeException("用户已存在");
        }

        allUsers.add(user);
        return Result.ok();
    }
}
