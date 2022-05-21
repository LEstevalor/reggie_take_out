package com.it.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.reggie.common.CustomException;
import com.it.reggie.dto.SetmealDto;
import com.it.reggie.mapper.SetmealMapper;
import com.it.reggie.pojo.Setmeal;
import com.it.reggie.pojo.SetmealDish;
import com.it.reggie.service.SetmealDishService;
import com.it.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service    //service层对应注解                       ↓对应dao接口    ↓对应实体           ↓对应service接口
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;
    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @Transactional  //事务控制（因为涉及多张表的操作——保证数据一致性）
    public void saveWithDish(SetmealDto setmealDto) {
        //插入setmeal表
        this.save(setmealDto);  //this是避免SetmealServiceImpl调用SetmealService成死环，SetmealDto继承了Setmeal

        //插入setmeal_dish表，但缺setmeal_id，老样子，lambda表达式
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐（删除&批量删除），同时参数与套餐相关的菜品的关联数据
     * @param ids
     */
    @Transactional  //事务控制（因为涉及多张表的操作——保证数据一致性）
    public void removeWithDish(List<Long> ids) {
        //查询套餐状态，是否停售 -> 可删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        // select count(*) from setmeal where id in (x1,x2,x3) and status = 1我们需要一个这样的查询，只要结果>0，抛异常
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);

        int count = this.count(queryWrapper);
        if (count > 0) {  //不可删，抛业务异常
            throw new CustomException("套餐正在售卖中，不能售卖");
        }                 //可删除↓

        //1、删除setmeal表中对应信息↓
        this.removeByIds(ids);  //ByIds对应批量删除

        //2、删除关系表setmeal_dish表的相关信息    [ids] setmeal.id -> setmeal_dish.setmeal_id
        LambdaQueryWrapper<SetmealDish> dishQueryWrapper = new LambdaQueryWrapper<>();
        // select * from setmeal_dish where id in (x1,x2,x3)
        dishQueryWrapper.in(SetmealDish::getId, ids);

        setmealDishService.remove(dishQueryWrapper);
    }
}
