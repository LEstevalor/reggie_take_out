package com.it.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.reggie.mapper.OrderDetailMapper;
import com.it.reggie.pojo.OrderDetail;
import com.it.reggie.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service  //service层注解
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
