package com.gee.mq.controller;

import com.gee.mq.bean.Result;
import com.gee.mq.bean.User;
import com.gee.mq.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/calc")
public class UserController {

    private final UserServiceImpl userService;


    /**
     * @description 登录
     *
     * @param user 用户
     * @return Result
     * @author Gee
     * @createTime 2022/7/11 20:39
     */
    @PostMapping("/login")
    public Result login(User user) {
        userService.login(user);
        return Result.ok();
    }

    /**
     * @description 注册
     *
     * @param user 用户
     * @return Result
     * @author Gee
     * @createTime 2022/7/11 20:39
     */
    @PostMapping("register")
    public Result register(User user) {
        userService.register(user);
        return Result.ok();
    }

}
