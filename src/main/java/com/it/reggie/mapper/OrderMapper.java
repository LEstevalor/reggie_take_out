package com.it.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.it.reggie.pojo.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper //接口注释
public interface OrderMapper extends BaseMapper<Orders> {
}
