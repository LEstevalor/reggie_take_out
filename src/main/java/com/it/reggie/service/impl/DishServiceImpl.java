package com.it.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.reggie.dto.DishDto;
import com.it.reggie.mapper.DishMapper;
import com.it.reggie.pojo.Dish;
import com.it.reggie.pojo.DishFlavor;
import com.it.reggie.service.DishFlavorService;
import com.it.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service    //service层对应注解                   ↓对应dao接口  ↓对应实体        ↓对应service接口
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;
    /**
     * 新增菜品，同时保存对应的口味数据
     * @param dishDto
     */
    @Transactional  //事务控制（因为涉及多张表的操作——保证数据一致性）
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品基本信息到dish表
        this.save(dishDto);   //为什么能直接传dishDto，因为DishDto是继承了Dish的

        Long dishId = dishDto.getId();  //易漏！dish_flavor是需要dish的ID，而前端原数据只是传入flavor基本信息，缺了dishId

        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        /*for (DishFlavor flavor : flavors) {   //普通人写法
                   flavor.setDishId(dishId);
        }*/

        //保存菜品口味数据到dish_flavor口味表（这里就需要dish_flavor的service层了）
        dishFlavorService.saveBatch(flavors);   //saveBatch -> 批量保存
    }

    /**
     * 根据id查询菜品信息和对应口味
     * @param id
     * @return
     */
    public DishDto getByIdWithFlavor(Long id) {
        Dish dish = this.getById(id);    //注意这里不能取创建DishService dishService（会变成死环->运行错误）

        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper); //where id = this.id返回多个口味，所以是集合，MP用list查询
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    /**
     * 修改菜品
     * （将菜品信息分别编辑到dish表和dish_flavor表）
     * @param dishDto
     */
    @Transactional  //易漏：事务控制（因为涉及多张表的操作——保证数据一致性）
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表的基本信息
        this.updateById(dishDto);  //this是为了避免在DishServiceImpl调用DishService陷入死环，dishDto是因为继承了Dish

        //dish_flavor对当前菜品（对应dish_id） -> 清理delete 后 添加insert
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        List<DishFlavor> flavors = dishDto.getFlavors();  //会发现这里有一个问题：dish_id没有跟上来，我往哪里插（这时候再补一个lambda表达式（优）或for循环）
        flavors = flavors.stream().map((item) -> {
           item.setDishId(dishDto.getId());    //把dish_id跟上去，再执行插入，才合理
           return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }
}
