package com.gee.mq.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gee.mq.bean.Aunt;
import com.gee.mq.bean.Result;
import com.gee.mq.service.AuntService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/aunt/calc")
public class AuntController {
    private final AuntService auntService;

    @PostMapping("/list")
    public Result queryAuntList(@RequestBody Aunt aunt) {
        List<Aunt> list = auntService.list(new LambdaQueryWrapper<Aunt>()
                .eq(Aunt::getUserId, aunt.getUserId())
                .orderByAsc(Aunt::getAuntDate));
        return Result.ok(list);
    }


    @PostMapping("/save")
    public Result saveAunt(@RequestBody Aunt aunt) {
        auntService.saveAunt(aunt);
        return Result.ok();
    }
}
