package com.gee.mq.service;

import cn.hutool.crypto.digest.MD5;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gee.mq.bean.User;
import com.gee.mq.config.GeeException;
import com.gee.mq.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> {
    public void login(User user) {
        User one = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, user.getUsername()));
        if (one == null) {
            throw new GeeException("账号不存在");
        }

        if (!one.getPassword().equals(MD5.create().digestHex(user.getPassword() + one.getSalt()))) {
            throw new GeeException("密码错误");
        }
    }

    public void register(User user) {
        if (user.getUsername() == null) {
            throw new GeeException("账号不能为空");
        }

        if (user.getPassword() == null) {
            throw new GeeException("密码不能为空");
        }

        if (user.getName() == null) {
            throw new GeeException("姓名不能为空");
        }

        User one = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, user.getUsername()));
        if (one != null) {
            throw new GeeException("账号已注册");
        }

        String salt = generaRandomStr(6);

        user.setSalt(salt);
        user.setPassword(MD5.create().digestHex(user.getPassword() + user.getSalt()));
        save(user);
    }

    private String generaRandomStr(int length) {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            stringBuilder.append(random.nextInt(10));
        }
        return stringBuilder.toString();
    }
}
