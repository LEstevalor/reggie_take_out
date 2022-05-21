package com.it.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.reggie.mapper.ShoppingCartMapper;
import com.it.reggie.pojo.ShoppingCart;
import com.it.reggie.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service  //服务层注解
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
