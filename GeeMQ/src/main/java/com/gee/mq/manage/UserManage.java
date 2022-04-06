package com.gee.mq.manage;

import com.gee.mq.bean.Result;
import com.gee.mq.bean.User;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Data

@RestController
@RequiredArgsConstructor
@RequestMapping("user")
public class UserManage {
    @PostMapping("login")
    private Result login(User user) {
        if ("admin".equals(user.getUsername()) && "123456".equals(user.getPassword())) {
            return Result.ok();
        } else {
            throw new RuntimeException("账号或密码错误");
        }
    }
}
