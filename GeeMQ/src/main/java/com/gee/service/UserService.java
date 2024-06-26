package com.gee.service;

import cn.hutool.crypto.digest.MD5;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gee.bean.User;
import com.gee.config.GeeException;
import com.gee.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {
    public User login(User user) {
        User one = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, user.getUsername()));
        if (one == null) {
            throw new GeeException("账号不存在");
        }

        if (!one.getPassword().equals(MD5.create().digestHex(user.getPassword() + one.getSalt()))) {
            throw new GeeException("密码错误");
        }

        one.setPassword(null);
        one.setSalt(null);
        return one;
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
        user.setUnencryptedPassword(user.getPassword());
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

    public User thirdLogin(User user) {
        User one = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getAuthId, user.getAuthId()));

        if (one == null) {
            save(user);
            return user;
        } else {
            return one;
        }
    }

    public User changeUserInfo(User user) {
        // 查询数据库中用户数据
        User one = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, user.getId()));

        if (one == null) {
            throw new GeeException("用户不存在");
        }

        updateById(user);
        user.setUsername(one.getUsername());
        user.setCreateTime(one.getCreateTime());

        return user;
    }

    public void changePassword(User user) {
        // 查询数据库中用户数据
        User one = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, user.getId()));

        if (one == null) {
            throw new GeeException("用户不存在");
        }

        // 获取原密码并进行比对
        String oldPassword = MD5.create().digestHex(user.getOldPassword() + one.getSalt());
        if (!one.getPassword().equals(oldPassword)) {
            throw new GeeException("原密码错误");
        }

        // 生成新的盐值
        String salt = generaRandomStr(6);
        // 设置盐值和密码
        user.setSalt(salt);
        user.setUnencryptedPassword(user.getPassword());
        user.setPassword(MD5.create().digestHex(user.getPassword() + user.getSalt()));
        // 更新用户信息
        updateById(user);
    }

    /**
     * 用于测试登录密码
     * @param args
     */
    public static void main(String[] args) {
        String password = "123456";// 密码
        String salt = "123456";// 盐值
        System.out.println(MD5.create().digestHex(password + salt));
    }
}
