package com.it.reggie.colltroller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.it.reggie.common.BaseContext;
import com.it.reggie.common.R;
import com.it.reggie.dto.DishDto;
import com.it.reggie.pojo.ShoppingCart;
import com.it.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController  //@ResponseBody + @Controller
@Slf4j  //日志
@RequestMapping("/shoppingCart")  //URL
public class ShoppingCartController {
    @Autowired  //spring填充（service层注释且继承完成就能被调用）
    private ShoppingCartService shoppingCartService;

    /**
     * 获取购物车内商品的集合
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        log.info("查看购物车...");
        //添加用户名、创建排序如购物车
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);

        return R.success(shoppingCartService.list(queryWrapper));
    }

    /**
     * 添加菜品到购物车
     * @return （注意前端传入信息，）
     */
    @PostMapping("/add")
    public R<String> save(@RequestBody ShoppingCart shoppingCart) {
        log.info("购物车数据:{}",shoppingCart);
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        //是否已添加过（因为购物车可以选数量）
        //这里有多个步骤：1、用户ID；2、是菜品还是套餐（dish_id和setmeal_id只能存在一个不是null）;
        //1
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);
        //2
        if(null != shoppingCart.getDishId()){ //添加到购物车的是菜品
            queryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }else{                                //添加到购物车的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        //SQL：select * from shopping_cart where user_id = ? and dish_id/setmeal_id = ?
        ShoppingCart one = shoppingCartService.getOne(queryWrapper);
        if (one != null) {
            //如果已添加，那么在原本的基础上加一
            one.setNumber(one.getNumber() + 1);
            shoppingCartService.updateById(one);  //注意不能搞成添加->唯一id重复
        } else {  //因数据库已默认number=1，创建时间不能是公共字段(要和更新时间一起用)
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
        }
        return R.success("添加成功");
    }

    /**
     * 购物车中修改商品 （实际上只对“-”起作用）（注意前端仅传回dish_id/setmeal_id，）
     * @return
     */
    @PostMapping("/sub")
    public R<String> update(@RequestBody ShoppingCart shoppingCart) {
        //select * from shopping_cart where dish_id/setmeal_id = ? and user_id = ?
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        if (null != shoppingCart.getDishId()) {
            queryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        } else {
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        ShoppingCart cartOne = shoppingCartService.getOne(queryWrapper);

        if (cartOne.getNumber() - 1 == 0) {
            //减后为0，删除
            shoppingCartService.removeById(cartOne);
        } else {
            //update shopping_cart set number = ? where id = ?（查询到的number-1）
            cartOne.setNumber(cartOne.getNumber() - 1);
            shoppingCartService.updateById(cartOne);
        }
        return R.success("修改成功");
    }

    /**
     * 购物车清空
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean() {
        //SQL:delete from shopping_cart where user_id = ?
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());

        shoppingCartService.remove(queryWrapper);
        return R.success("清空购物车成功");
    }
}
