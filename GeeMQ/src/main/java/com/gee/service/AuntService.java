package com.gee.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gee.bean.Aunt;
import com.gee.bean.EchartsData;
import com.gee.bean.User;
import com.gee.config.GeeException;
import com.gee.mapper.AuntMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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

    public List<LocalDate> getAuntLocalDate(User user) {
        List<Aunt> auntList = list(new LambdaQueryWrapper<Aunt>()
                .eq(Aunt::getUserId, user.getId())
                .orderByAsc(Aunt::getAuntDate));
        return auntList.stream().map(item -> {
            Date auntDate = item.getAuntDate();
            return auntDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }).collect(Collectors.toList());
    }

    public List<String> auntInterval(List<LocalDate> list) {
        List<String> auntInterval = new ArrayList<>(list.size() - 1);
        if (list.size() >= 2) {
            for (int i = 0; i < list.size() - 1; i++) {
                LocalDate before = list.get(i);
                LocalDate after = list.get(i + 1);
                long until = before.until(after, ChronoUnit.DAYS);

                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月");
                auntInterval.add(before.format(dateTimeFormatter) + "到" + after.format(dateTimeFormatter) + "相差" + until + "天");
            }
        } else {
            auntInterval.add("此功能需保存两次及以上记录");
        }

        return auntInterval;
    }

    public Map<String, Object> auntRecord(List<LocalDate> list) {
        LocalDate first = list.get(0);
        LocalDate second = list.get(1);

        Map<String, Object> result = new HashMap<>();
        long min = first.until(second, ChronoUnit.DAYS);
        long max = min;
        long sum = max;

        if (list.size() >= 2) {
            for (int i = 1; i < list.size() - 1; i++) {
                long until = getDaySubtract(list, i);
                if (until < min) {
                    min = until;
                }

                if (until > max) {
                    max = until;
                }
                sum += until;
            }
            TreeMap<Long, Long> countWithDays = getCountWithDays(list);
            AtomicLong maxCount = new AtomicLong(0L);
            Map<Long, BigDecimal> percentData = new HashMap<>();
            StringBuilder daysMax = new StringBuilder();
            List<String> dayCount = new ArrayList<>();
            countWithDays.forEach((day, count) -> {
                if (count > maxCount.get()) {
                    maxCount.set(count);
                }
                dayCount.add(day + "天出现" + count + "次");
            });

            countWithDays.forEach((day, count) -> {
                if (count.equals(maxCount.get())) {
                    daysMax.append(day).append("天,");
                }
            });

            daysMax.replace(daysMax.length() - 1, daysMax.length(), "")
                    .append("出现次数最多，次数为").append(maxCount).append("次");

            BigDecimal avg = new BigDecimal(sum).divide(new BigDecimal(list.size() - 1), 2, RoundingMode.HALF_UP);
            LocalDate nextDay = list.get(list.size() - 1).plusDays(
                    new BigDecimal(sum).divide(new BigDecimal(list.size() - 1), 0, RoundingMode.HALF_UP).longValue());
            result.put("min", "最小间隔: " + min + "天");
            result.put("max", "最大间隔: " + max + "天");
            result.put("avg", "平均间隔: " + avg + "天");
            result.put("daysMax", daysMax);
            result.put("dayCount", dayCount);
            result.put("nextDays", "预计下次时间为" + nextDay.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")));
            result.put("percentData", percentData);
        } else {

            result.put("message", "此功能需保存两次及以上记录");
        }
        return result;
    }

    public Map<String, Object> echarts(List<LocalDate> list) {
        HashMap<String, Object> result = new HashMap<>();

        // 天数占比
        List<EchartsData> daysPercentList = new ArrayList<>();
        TreeMap<Long, Long> countWithDays = getCountWithDays(list);
        BigDecimal hundred = new BigDecimal(100);
        countWithDays.forEach((day, count) -> {
            BigDecimal percent = new BigDecimal(count).divide(new BigDecimal(list.size() - 1), 4, RoundingMode.HALF_UP).multiply(hundred);
            EchartsData echartsData = new EchartsData(day + "天", percent);
            daysPercentList.add(echartsData);
        });
        BigDecimal lastPercent = new BigDecimal(100);
        for (int i = 0; i < daysPercentList.size() - 1; i++) {
            BigDecimal percent = daysPercentList.get(i).getValue();
            lastPercent = lastPercent.subtract(percent);
        }
        EchartsData echartsDataLast = daysPercentList.get(daysPercentList.size() - 1);
        echartsDataLast.setValue(lastPercent);
        result.put("daysPercentList", daysPercentList);

        // 天数趋势
        List<EchartsData> daysTrendList = new ArrayList<>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月");
        if (list.size() > 8) {
            for (int i = list.size() - 8; i < list.size() - 1; i++) {
                long daySubtract = getDaySubtract(list, i);
                EchartsData echartsData = new EchartsData(list.get(i).format(dateTimeFormatter), new BigDecimal(daySubtract));
                daysTrendList.add(echartsData);
            }
        } else {
            for (int i = 0; i < list.size() - 1; i++) {
                long daySubtract = getDaySubtract(list, i);
                EchartsData echartsData = new EchartsData(list.get(i).format(dateTimeFormatter), new BigDecimal(daySubtract));
                daysTrendList.add(echartsData);
            }
        }

        result.put("daysTrendList", daysTrendList);
        return result;
    }

    public TreeMap<Long, Long> getCountWithDays(List<LocalDate> list) {
        TreeMap<Long, Long> countWithDays = new TreeMap<>();
        for (int i = 0; i < list.size() - 1; i++) {
            long until = getDaySubtract(list, i);
            Long value = countWithDays.get(until);
            if (value == null) {
                countWithDays.put(until, 1L);
            } else {
                countWithDays.put(until, value + 1);
            }
        }
        return countWithDays;
    }

    private long getDaySubtract(List<LocalDate> list, int i) {
        LocalDate before = list.get(i);
        LocalDate after = list.get(i + 1);
        return before.until(after, ChronoUnit.DAYS);
    }
}
