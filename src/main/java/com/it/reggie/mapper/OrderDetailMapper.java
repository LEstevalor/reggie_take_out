package com.it.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.it.reggie.pojo.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

@Mapper  //接口注释
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {
}
