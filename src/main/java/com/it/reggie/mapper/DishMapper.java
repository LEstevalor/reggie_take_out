package com.it.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.it.reggie.pojo.Dish;
import org.apache.ibatis.annotations.Mapper;

@Mapper //dao层注解
public interface DishMapper extends BaseMapper<Dish> {
}
