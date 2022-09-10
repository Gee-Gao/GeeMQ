package com.gee.controller;

import com.gee.bean.Result;
import com.gee.bean.User;
import com.gee.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/calc")
public class UserController {

    private final UserService userService;


    /**
     * @description 登录
     *
     * @param user 用户
     * @return Result
     * @author Gee
     * @createTime 2022/7/11 20:39
     */
    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        User login = userService.login(user);
        return Result.ok(login);
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
    public Result register(@RequestBody User user) {
        userService.register(user);
        return Result.ok();
    }

    @PostMapping("thirdLogin")
    private Result thirdLogin(@RequestBody User user) {
        User login = userService.wxLogin(user);
        return Result.ok(login);
    }

}
