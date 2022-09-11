package com.gee.controller;

import com.gee.bean.Aunt;
import com.gee.bean.Result;
import com.gee.bean.User;
import com.gee.service.AuntService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/aunt/calc")
public class AuntController {
    private final AuntService auntService;

    @PostMapping("/list")
    public Result queryAuntList(@RequestBody Aunt aunt) {
        List<Aunt> list =  auntService.queryAuntList(aunt);
        return Result.ok(list);
    }


    @PostMapping("/save")
    public Result saveAunt(@RequestBody Aunt aunt) {
        auntService.saveAunt(aunt);
        return Result.ok();
    }

    @PostMapping("/auntInterval")
    public Result analyzerAuntInterval(@RequestBody User user) {
        List<LocalDate> list = auntService.getAuntLocalDate(user);
        return Result.ok(auntService.auntInterval(list));
    }

    @PostMapping("/auntRecord")
    public Result analyzerAuntRecord(@RequestBody User user) {
        List<LocalDate> list = auntService.getAuntLocalDate(user);
        return Result.ok(auntService.auntRecord(list));
    }

    @PostMapping("/echarts")
    public Result echarts(@RequestBody User user) {
        List<LocalDate> list = auntService.getAuntLocalDate(user);
        return Result.ok(auntService.echarts(list));
    }

    @PostMapping("/delete")
    public Result deleteAunt(@RequestBody Aunt aunt) {
        auntService.removeById(aunt);
        return Result.ok();
    }

}
