package com.gee.mq.manage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

public class AuntCalc {
    public static void main(String[] args) throws IOException {
        String dateStr;
        List<LocalDate> list = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        BufferedReader br = new BufferedReader(new FileReader("D:/姨妈时间.txt"));

        while ((dateStr = br.readLine()) != null) {
            LocalDate localDate = LocalDate.parse(dateStr, formatter);
            list.add(localDate);
        }

        LocalDate first = list.get(0);
        LocalDate second = list.get(1);
        long min = first.until(second, ChronoUnit.DAYS);
        long max = min;

        long sum = 0L;

        TreeMap<Long, Long> countWithDays = new TreeMap<>();

        System.out.println("-------------------------");
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
            System.out.println(before.getYear() + "年" + before.getMonthValue() + "月到" + after.getMonthValue() + "月相差" + until + "天");
        }
        System.out.println("-------------------------");
        AtomicLong maxDay = new AtomicLong(0L);
        AtomicLong maxCount = new AtomicLong(0L);
        countWithDays.forEach((day, count) -> {
            if (count > maxCount.get()) {
                maxCount.set(count);
                maxDay.set(day);
            }
            System.out.println(day + "天出现" + count + "次");
        });
        System.out.println(maxDay.get() + "天出现次数最多,次数为: " + maxCount + "次");
        System.out.println("-------------------------");
        BigDecimal average = new BigDecimal(sum).divide(new BigDecimal(list.size() - 1), 2, RoundingMode.HALF_UP);
        System.out.println("最小间隔: " + min + "天");
        System.out.println("最大间隔: " + max + "天");
        System.out.println("平均间隔: " + average + "天");
        BigDecimal nextDays = new BigDecimal(sum).divide(new BigDecimal(list.size() - 1), 0, RoundingMode.HALF_UP);
        System.out.println("预计下次" + list.get(list.size() - 1).plusDays(nextDays.longValue()));
        System.out.println("-------------------------");
    }
}