package com.it.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.reggie.dto.DishDto;
import com.it.reggie.mapper.DishFlavorMapper;
import com.it.reggie.pojo.Dish;
import com.it.reggie.pojo.DishFlavor;
import com.it.reggie.service.DishFlavorService;
import com.it.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service   //service层对应注解                              ↓对应dao接口    ↓对应实体             ↓对应service接口
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper,DishFlavor> implements DishFlavorService {
}
