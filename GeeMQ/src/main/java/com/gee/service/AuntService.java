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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class AuntService extends ServiceImpl<AuntMapper, Aunt> {

    /**
     * @description 保存姨妈
     *
	 * @param aunt 姨妈参数
     * @author Gee
     * @createTime 2022/9/18 1:31
     */
    public void saveAunt(Aunt aunt) {
        if (aunt.getAuntDate() == null) {
            throw new GeeException("未选择姨妈日期");
        }

        Aunt one = getOne(new LambdaQueryWrapper<Aunt>()
                .eq(Aunt::getAuntDate, aunt.getAuntDate())
                .eq(Aunt::getUserId, aunt.getUserId()));

        if (one != null) {
            throw new GeeException("此记录已存在");
        }

        save(aunt);

    }

    /**
     * @description 获取LocalDate类型的姨妈日期
     *
	 * @param user 登录用户
     * @return List<LocalDate> 姨妈日期
     * @author Gee
     * @createTime 2022/9/18 1:32
     */
    public List<LocalDate> getAuntLocalDate(User user) {
        List<Aunt> auntList = list(new LambdaQueryWrapper<Aunt>()
                .eq(Aunt::getUserId, user.getId())
                .orderByAsc(Aunt::getAuntDate));
        return auntList.stream().map(item -> {
            Date auntDate = item.getAuntDate();
            return auntDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }).collect(Collectors.toList());
    }

    /**
     * @description 获取姨妈间隔
     *
	 * @param list 姨妈日期列表
     * @return List<String> 姨妈间隔
     * @author Gee
     * @createTime 2022/9/18 1:33
     */
    public List<String> auntInterval(List<LocalDate> list) {
        List<String> auntInterval = new ArrayList<>();
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

    /**
     * @description 姨妈分析
     *
	 * @param list 姨妈日期列表
     * @return Map< String,Object> 姨妈分析结果
     * @author Gee
     * @createTime 2022/9/18 1:35
     */
    public Map<String, Object> auntRecord(List<LocalDate> list) {
        Map<String, Object> result = new HashMap<>();
        if (list.size() < 2) {
            result.put("message", "此功能需保存两次及以上记录");
            return result;
        }

        LocalDate first = list.get(0);
        LocalDate second = list.get(1);

        long min = first.until(second, ChronoUnit.DAYS);
        long max = min;
        long sum = max;


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
        return result;
    }


    /**
     * @description 图表分析
     *
     * @param list 姨妈日期列表
     * @return Map< String,Object> 图表分析结果
     * @author Gee
     * @createTime 2022/9/18 1:35
     */
    public Map<String, Object> echarts(List<LocalDate> list) {
        HashMap<String, Object> result = new HashMap<>();
        if (list.size() < 2) {
            result.put("message", "此功能需保存两次及以上记录");
            return result;
        }
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

    /**
     * @description 获取姨妈天数出现次数
     *
     * @param list 姨妈日期列表
     * @return TreeMap<Long, Long> 姨妈天数出现次数
     * @author Gee
     * @createTime 2022/9/18 1:35
     */
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

    /**
     * @description 获取姨妈时间差
     *
     * @param list 姨妈日期列表
     * @param i 第几个元素
     * @return long 姨妈时间差
     * @author Gee
     * @createTime 2022/9/18 1:35
     */
    private long getDaySubtract(List<LocalDate> list, int i) {
        LocalDate before = list.get(i);
        LocalDate after = list.get(i + 1);
        return before.until(after, ChronoUnit.DAYS);
    }

    /**
     * @description 查询姨妈列表
     *
	 * @param aunt 姨妈参数
     * @return List<Aunt> 姨妈列表
     * @author Gee
     * @createTime 2022/9/18 1:38
     */
    public List<Aunt> queryAuntList(Aunt aunt) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
        List<Aunt> list = list(new LambdaQueryWrapper<Aunt>()
                .eq(Aunt::getUserId, aunt.getUserId())
                .orderByAsc(Aunt::getAuntDate));
        list.forEach(item -> item.setAuntDateStr(dateFormat.format(item.getAuntDate())));
        return list;
    }
}
