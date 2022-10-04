package com.gee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gee.bean.Aunt;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

public interface AuntMapper extends BaseMapper<Aunt> {


    @Select("SELECT  max(aunt_date) as auntDate from  aunt where user_id = #{userId}")
    LocalDate selectLastAunt(String userId);
}