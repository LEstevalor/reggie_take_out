package com.it.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.reggie.common.CustomException;
import com.it.reggie.mapper.CategoryMapper;
import com.it.reggie.pojo.Category;
import com.it.reggie.pojo.Dish;
import com.it.reggie.pojo.Setmeal;
import com.it.reggie.service.CategoryService;
import com.it.reggie.service.DishService;
import com.it.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service   //service层对应注解                          ↓对应dao接口    ↓对应实体             ↓对应service接口
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id删除分类，删除之前需要进行判断
     * @param id
     */
    @Override
    public void remove(Long id) {
        //查询当前分类是否关联菜品，如果关联，抛出一个业务异常
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询添加，根据分类id进行查询
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        int count = dishService.count(dishLambdaQueryWrapper);

        if (count > 0) {
            //已关联菜品，抛业务异常
            throw new CustomException("当前分类下关联菜品，不能删除");
        }

        //查询当前分类是否关联套餐，如果关联，抛出一个业务异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询添加，根据分类id进行查询
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);

        if (count2 > 0) {
            //已关联套餐，抛业务异常
            throw new CustomException("当前分类下关联套餐，不能删除");
        }

        //正常删除分类（前面都验证完，说明就没有关联）
        super.removeById(id);
    }
}
