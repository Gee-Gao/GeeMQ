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

        long sum = 0L;
        for (int i = 0; i < list.size() - 1; i++) {
            LocalDate before = list.get(i);
            LocalDate after = list.get(i + 1);
            long until = before.until(after, ChronoUnit.DAYS);
            sum += until;
            System.out.println(before.getYear() + "年" + before.getMonthValue() + "月到" + after.getMonthValue() + "月相差" + until + "天");
        }

        BigDecimal average = new BigDecimal(sum).divide(new BigDecimal(list.size() - 1), 2, RoundingMode.HALF_UP);
        System.out.println("平均间隔：" + average+"天");
        BigDecimal nextDays = new BigDecimal(sum).divide(new BigDecimal(list.size() - 1), 0, RoundingMode.HALF_UP);
        System.out.println("预计下次" + list.get(list.size() - 1).plusDays(nextDays.longValue()));
    }
}