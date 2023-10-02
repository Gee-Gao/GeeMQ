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

    /**
     * @description 第三方登录
     *
     * @param user 用户
     * @return Result
     * @author Gee
     * @createTime 2022/7/11 20:39
     */
    @PostMapping("thirdLogin")
    public Result thirdLogin(@RequestBody User user) {
        User login = userService.thirdLogin(user);
        return Result.ok(login);
    }


    /**
     * @description 修改用户信息
     *
     * @param user 用户
     * @return Result
     * @author Gee
     * @createTime 2022/7/11 20:39
     */
    @PostMapping("/changeUserInfo")
    public Result changeUserInfo(@RequestBody User user) {
        User changeUserInfo = userService.changeUserInfo(user);
        return Result.ok(changeUserInfo);
    }

    /**
     * @description 修改密码
     *
     * @param user 用户
     * @return Result
     * @author Gee
     * @createTime 2022/7/11 20:39
     */
    @PostMapping("/changePassword")
    public Result changePassword(@RequestBody User user) {
        userService.changePassword(user);
        return Result.ok();
    }
}
