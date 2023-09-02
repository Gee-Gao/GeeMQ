package com.gee.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gee.bean.Aunt;
import com.gee.bean.AuntAnalyzer;
import com.gee.bean.EchartsData;
import com.gee.config.GeeException;
import com.gee.mapper.AuntMapper;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class AuntService extends ServiceImpl<AuntMapper, Aunt> {

    private final AuntMapper auntMapper;

    private final AuntAnalyzerService auntAnalyzerService;

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
        List<LocalDate> auntLocalDate = getAuntLocalDate(aunt);
        auntAnalyzer(auntLocalDate,aunt.getUserId());
    }

    /**
     * @description 获取LocalDate类型的姨妈日期
     *
	 * @param aunt 姨妈参数
     * @return List<LocalDate> 姨妈日期
     * @author Gee
     * @createTime 2022/9/18 1:32
     */
    public List<LocalDate> getAuntLocalDate(Aunt aunt) {
        List<Aunt> auntList = list(new LambdaQueryWrapper<Aunt>()
                .eq(Aunt::getUserId, aunt.getUserId())
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
            for (int i = list.size() - 1; i > 0; i--) {
                LocalDate before = list.get(i);
                LocalDate after = list.get(i - 1);
                long until = before.until(after, ChronoUnit.DAYS);

                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月");
                auntInterval.add(before.format(dateTimeFormatter) + "到" + after.format(dateTimeFormatter) + "相差" + -until + "天");
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
     * @param userId 登录用户id
     * @author Gee
     * @createTime 2022/9/18 1:35
     */
    public void auntAnalyzer(List<LocalDate> list, String userId) {
        AuntAnalyzer auntAnalyzer = auntAnalyzerService.getOne(new LambdaQueryWrapper<AuntAnalyzer>()
                .eq(AuntAnalyzer::getUserId, userId));

        if (list.size() < 2) {
            if(auntAnalyzer ==null) {
                auntAnalyzer = new AuntAnalyzer();
                auntAnalyzer.setUserId(userId);
                auntAnalyzer.setMessage("此功能需保存两次及以上记录");
                auntAnalyzerService.save(auntAnalyzer);
            } else {
                auntAnalyzer.setMessage("此功能需保存两次及以上记录");
                auntAnalyzerService.updateById(auntAnalyzer);
            }
            return;
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
        if(auntAnalyzer==null){
            auntAnalyzer = new AuntAnalyzer();
            appendAnalyzerData(auntAnalyzer, min, max, daysMax, dayCount, avg, nextDay);
            auntAnalyzer.setUserId(userId);
            auntAnalyzer.setMessage("");
            auntAnalyzerService.save(auntAnalyzer);
        }else {
            appendAnalyzerData(auntAnalyzer, min, max, daysMax, dayCount, avg, nextDay);
            auntAnalyzer.setMessage("");
            auntAnalyzerService.updateById(auntAnalyzer);
        }


    }

    /**
     * @description 组装姨妈分析对象
     *
	 * @param auntAnalyzer 待保存的姨妈分析对象
	 * @param min 最小间隔
	 * @param max 最大间隔
	 * @param daysMax 出现次数最多的天数
	 * @param dayCount 天数出现次数
	 * @param avg 平均间隔
	 * @param nextDay 下一次姨妈日期
     * @author Gee
     * @createTime 2022/9/25 15:46
     */
    private void appendAnalyzerData(AuntAnalyzer auntAnalyzer, long min, long max, StringBuilder daysMax, List<String> dayCount, BigDecimal avg, LocalDate nextDay) {
        auntAnalyzer.setMinDay("最小间隔: " + min + "天");
        auntAnalyzer.setMaxDay("最大间隔: " + max + "天");
        auntAnalyzer.setAvgDay("平均间隔: " + avg + "天");
        auntAnalyzer.setDaysMax(daysMax.toString());
        auntAnalyzer.setDayCount(dayCount.stream().map(String::valueOf).collect(Collectors.joining(",")));
        auntAnalyzer.setNextDate(nextDay);
        auntAnalyzer.setNextDayStr("预计下次时间为" + nextDay.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")));
        LocalDate ovulation = nextDay.plusDays(-14L);
        auntAnalyzer.setOvulation(ovulation);
        auntAnalyzer.setOvulationStr("预计下次排卵日为"+ovulation.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")));
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
            EchartsData echartsData = new EchartsData(day + "天:\n"+ percent.setScale(2,RoundingMode.HALF_UP)+"%" , percent);
            daysPercentList.add(echartsData);
        });

        // 计算最后一次的百分比
        BigDecimal lastPercent = new BigDecimal(100);
        for (int i = 0; i < daysPercentList.size() - 1; i++) {
            BigDecimal percent = daysPercentList.get(i).getValue();
            lastPercent = lastPercent.subtract(percent);
        }
        EchartsData echartsDataLast = daysPercentList.get(daysPercentList.size() - 1);
        String[] split = echartsDataLast.getName().split("\n");
        echartsDataLast.setName(split[0] +'\n'+lastPercent.setScale(2,RoundingMode.HALF_UP)+"%");
        echartsDataLast.setValue(lastPercent);

        result.put("daysPercentList", daysPercentList);

        // 半年内天数趋势
        List<EchartsData> daysTrendList = new ArrayList<>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月");
        if (list.size() > 7) {
            for (int i = list.size() - 7; i < list.size() - 1; i++) {
                long daySubtract = getDaySubtract(list, i);
                EchartsData echartsData = new EchartsData(list.get(i + 1).format(dateTimeFormatter), new BigDecimal(daySubtract));
                daysTrendList.add(echartsData);
            }
        } else {
            for (int i = 0; i < list.size() - 1; i++) {
                long daySubtract = getDaySubtract(list, i);
                EchartsData echartsData = new EchartsData(list.get(i + 1).format(dateTimeFormatter), new BigDecimal(daySubtract));
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
                .orderByDesc(Aunt::getAuntDate));
        list.forEach(item -> item.setAuntDateStr(dateFormat.format(item.getAuntDate())));
        return list;
    }

    /**
     * @description 获取安全期和危险期
     *
     * @param aunt 姨妈参数
     * @return Map<String, Object> 安全期和危险期
     * @author Gee
     * @createTime 2022/9/18 11:05bu
     */
    public Map<String, Object> calendar(Aunt aunt) {
        AuntAnalyzer one = auntAnalyzerService.getOne(new LambdaQueryWrapper<AuntAnalyzer>()
                .eq(AuntAnalyzer::getUserId, aunt.getUserId()));
        Map<String, Object> result = new HashMap<>(2);
        if(one == null){
            result.put("message", "此功能需保存一次记录");
        }else {
            // 获取排卵日
            LocalDate ovulation = one.getOvulation();

            if (ovulation == null) {
                ovulation = auntMapper.selectLastAunt(aunt.getUserId());
                if(ovulation == null){
                    result.put("message", "此功能需保存一次记录");
                    return result;
                }
            }
            // 计算危险期
            List<LocalDate> dangerousPeriod = new ArrayList<>(14);
            for (int i = 5; i >=0 ; i--) {
                dangerousPeriod.add(ovulation.plusDays(-i));
            }
            for (int i = 1; i <= 4; i++) {
                dangerousPeriod.add(ovulation.plusDays(i));
            }
            result.put("dangerousPeriod",dangerousPeriod);
        }
        return result;
    }

    /**
     * @description 姨妈分析
     *
	 * @param aunt 姨妈参数
     * @return Map<String, Object>
     * @author Gee
     * @createTime 2022/9/18 16:59
     */
    public Map<String, Object> auntAnalyzer(Aunt aunt) {
        Map<String, Object> result = new HashMap<>();
        AuntAnalyzer auntAnalyzer = auntAnalyzerService.getOne(new LambdaQueryWrapper<AuntAnalyzer>()
                .eq(AuntAnalyzer::getUserId, aunt.getUserId()));
        if (auntAnalyzer == null) {
            result.put("message", "此功能需保存两次及以上记录");
        } else {
            if (StrUtil.isBlank(auntAnalyzer.getMessage())) {
                result.put("min", auntAnalyzer.getMinDay());
                result.put("max", auntAnalyzer.getDaysMax());
                result.put("avg", auntAnalyzer.getAvgDay());
                result.put("daysMax", auntAnalyzer.getDaysMax());
                String[] dayCount = auntAnalyzer.getDayCount().split(",");
                result.put("dayCount", dayCount);
                result.put("nextDayStr", auntAnalyzer.getNextDayStr());
                result.put("nextOvulationDayStr",auntAnalyzer.getOvulationStr());

            } else {
                result.put("message", auntAnalyzer.getMessage());
            }
        }
        return result;
    }

    public void deleteAunt(Aunt aunt) {
        removeById(aunt);
        List<LocalDate> auntLocalDate = getAuntLocalDate(aunt);
        auntAnalyzer(auntLocalDate,aunt.getUserId());
    }
}
