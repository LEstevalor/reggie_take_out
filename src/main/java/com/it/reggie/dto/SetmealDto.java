package com.it.reggie.dto;

import com.it.reggie.pojo.Dish;
import com.it.reggie.pojo.Setmeal;
import com.it.reggie.pojo.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;

    //要的是Dish的图片（加入该属性，原因：移动端点击套餐时需实现套餐菜品，而Setmeal与SermealDish中均无image属性返回）
    private String image;
}
