package com.it.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.it.reggie.pojo.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

@Mapper //接口注释
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {
}
