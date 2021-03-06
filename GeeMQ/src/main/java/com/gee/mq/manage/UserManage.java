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
import java.util.stream.Collectors;

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

    /**
     * @description  修改密码
     *
	 * @param user 用户
     * @return Result
     * @author Gee
     * @createTime 2022/7/11 20:39
     */
    @PostMapping("changePassword")
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

    /**
     * @description  登录
     *
     * @param user 用户
     * @return Result
     * @author Gee
     * @createTime 2022/7/11 20:39
     */
    @PostMapping("login")
    public Result login(User user) {
        long count = allUsers.stream().filter(item -> item.equals(user)).count();
        if (count > 0) {
            return Result.ok();
        } else {
            throw new RuntimeException("账号或密码错误");
        }
    }

    /**
     * @description  注册
     *
     * @param user 用户
     * @return Result
     * @author Gee
     * @createTime 2022/7/11 20:39
     */
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

    /**
     * @description 删除用户
     *
	 * @param user 用户
     * @return Result
     * @author Gee
     * @createTime 2022/7/11 20:40
     */
    @PostMapping("deleteUser")
    public Result deleteUser(User user){
        long count = allUsers.stream().filter(item -> item.equals(user)).count();

        if(count==0){
            throw new RuntimeException("账号或密码错误");
        }

        allUsers= allUsers.stream()
                .filter(item -> !item.equals(user))
                .collect(Collectors.toList());

        return Result.ok();
    }

}
