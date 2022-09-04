package com.gee.mq.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gee.mq.bean.Aunt;
import com.gee.mq.bean.Result;
import com.gee.mq.bean.User;
import com.gee.mq.service.AuntService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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

    @PostMapping("/analyzer")
    public Result analyzer(@RequestBody User user) {
        List<LocalDate> list = getAuntLocalDate(user);
        Map<String, Object> result = calcAunt(list);
        return Result.ok(result);
    }

    private List<LocalDate> getAuntLocalDate(User user) {
        List<Aunt> auntList = auntService.list(new LambdaQueryWrapper<Aunt>()
                .eq(Aunt::getUserId, user.getId())
                .orderByAsc(Aunt::getAuntDate));
        return auntList.stream().map(item -> {
            Date auntDate = item.getAuntDate();
            return auntDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }).collect(Collectors.toList());
    }

    private Map<String, Object> calcAunt(List<LocalDate> list) {
        Map<String, Object> result = new HashMap<>(8);
        LocalDate first = list.get(0);
        LocalDate second = list.get(1);

        long min = first.until(second, ChronoUnit.DAYS);
        long max = min;
        long sum = 0L;
        // 姨妈间隔时间
        List<String> auntInterval = new ArrayList<>(list.size() - 1);
        // 姨妈出现次数
        TreeMap<Long, Long> countWithDays = new TreeMap<>();

        for (int i = 0; i < list.size() - 1; i++) {
            LocalDate before = list.get(i);
            LocalDate after = list.get(i + 1);
            long until = before.until(after, ChronoUnit.DAYS);
            Long value = countWithDays.get(until);
            if (value == null) {
                countWithDays.put(until, 1L);
            } else {
                countWithDays.put(until, value + 1);
            }

            if (until < min) {
                min = until;
            }
            if (until > max) {
                max = until;
            }
            sum += until;
            auntInterval.add(before.getYear() + "年" + before.getMonthValue() + "月到" + after.getMonthValue() + "月相差" + until + "天");
        }

        AtomicLong maxCount = new AtomicLong(0L);
        // 姨妈出现次数
        List<String> daysInterval = new ArrayList<>(countWithDays.size());
        countWithDays.forEach((day, count) -> {
            if (count > maxCount.get()) {
                maxCount.set(count);
            }
            daysInterval.add(day + "天出现" + count + "次");
        });
        StringBuilder daysMax = new StringBuilder();
        countWithDays.forEach((day, count) -> {
            if(count.equals(maxCount.get()) ){
                daysMax.append(day).append(",");
            }
        });
        daysMax.replace(daysMax.length()-1,daysMax.length(),"");
        daysMax.append("天出现次数最多,次数为: ").append(maxCount).append("次");
        BigDecimal avg = new BigDecimal(sum).divide(new BigDecimal(list.size() - 1), 2, RoundingMode.HALF_UP);
        BigDecimal nextAunt = new BigDecimal(sum).divide(new BigDecimal(list.size() - 1), 0, RoundingMode.HALF_UP);
        result.put("auntInterval", auntInterval);
        result.put("daysInterval", daysInterval);
        result.put("daysMax", daysMax);
        result.put("minInterval", min);
        result.put("maxInterval", max);
        result.put("avgInterval", avg);
        result.put("nextAunt", "预计下次" + list.get(list.size() - 1).plusDays(nextAunt.longValue()));
        return result;
    }

}
