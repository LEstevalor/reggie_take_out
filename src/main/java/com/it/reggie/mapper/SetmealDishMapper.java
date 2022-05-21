package com.it.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.it.reggie.pojo.Setmeal;
import com.it.reggie.pojo.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

@Mapper  //接口注释
public interface SetmealDishMapper extends BaseMapper<SetmealDish> {
}
