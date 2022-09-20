package com.gee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gee.bean.Aunt;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface AuntMapper extends BaseMapper<Aunt> {
    @Select("SELECT max(aunt_date) as auntDate FROM aunt WHERE user_id =#{aunt.userId}")
    Aunt calendar(@Param("aunt") Aunt aunt);
}