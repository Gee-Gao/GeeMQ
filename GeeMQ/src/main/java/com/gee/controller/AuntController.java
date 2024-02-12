package com.gee.controller;

import com.gee.bean.Aunt;
import com.gee.bean.Result;
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
        List<Aunt> list = auntService.queryAuntList(aunt);
        return Result.ok(list);
    }


    @PostMapping("/save")
    public Result saveAunt(@RequestBody Aunt aunt) {
        // 保存并获取距离当前时间最近的一次姨妈
        Aunt lastAunt = auntService.saveAunt(aunt);
        return Result.ok(lastAunt);
    }

    @PostMapping("/auntInterval")
    public Result analyzerAuntInterval(@RequestBody Aunt aunt) {
        List<LocalDate> list = auntService.getAuntLocalDate(aunt);
        return Result.ok(auntService.auntInterval(list));
    }

    @PostMapping("/auntAnalyzer")
    public Result analyzerAuntRecord(@RequestBody Aunt aunt) {
        return Result.ok(auntService.auntAnalyzer(aunt));
    }

    @PostMapping("/echarts")
    public Result echarts(@RequestBody Aunt aunt) {
        List<LocalDate> list = auntService.getAuntLocalDate(aunt);
        return Result.ok(auntService.echarts(list));
    }

    @PostMapping("/delete")
    public Result deleteAunt(@RequestBody Aunt aunt) {
        auntService.deleteAunt(aunt);
        return Result.ok();
    }

    @PostMapping("/calendar")
    public Result calendar(@RequestBody Aunt aunt) {
        return Result.ok(auntService.calendar(aunt));
    }


    @PostMapping("/getLastAunt")
    public Result getLastAunt(@RequestBody Aunt aunt) {
        return Result.ok(auntService.getLastAunt(aunt));
    }

    @PostMapping("/auntFixMonth")
    public Result everyMonth(@RequestBody Aunt aunt) {
        return Result.ok(auntService.auntFixMonth(aunt));
    }
}
