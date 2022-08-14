package com.gee.mq.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gee.mq.bean.Aunt;
import com.gee.mq.config.GeeException;
import com.gee.mq.mapper.AuntMapper;
import org.springframework.stereotype.Service;

@Service
public class AuntService extends ServiceImpl<AuntMapper, Aunt> {

    public void saveAunt(Aunt aunt) {
        if (aunt.getAuntDate() == null) {
            throw new GeeException("未选择姨妈日期");
        }

        Aunt one = getOne(new LambdaQueryWrapper<Aunt>().eq(Aunt::getAuntDate, aunt.getAuntDate()));

        if (one != null) {
            throw new GeeException("此记录已存在");
        }

        save(aunt);

    }
}
