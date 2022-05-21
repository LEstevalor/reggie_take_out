package com.it.reggie.colltroller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.reggie.common.R;
import com.it.reggie.dto.SetmealDto;
import com.it.reggie.pojo.Category;
import com.it.reggie.pojo.Dish;
import com.it.reggie.pojo.Setmeal;
import com.it.reggie.pojo.SetmealDish;
import com.it.reggie.service.CategoryService;
import com.it.reggie.service.DishService;
import com.it.reggie.service.SetmealDishService;
import com.it.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.el.lang.ELArithmetic;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */
@Slf4j   //日志
@RestController   //Controller层 + ResponseBody
@RequestMapping("/setmeal")
public class SetmealDishController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;   //分页查询数据需要分类名称
    @Autowired
    private DishService dishService;  //移动端点击套餐显示菜品，需要dish表的图片
    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "setmealCache", allEntries = true)   //表示删除setmealCache所有的数据（keys）
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("套餐信息：{}",setmealDto);
        //设计两张表的操作，与之前的操作相同，我们把方法体写到service层
        setmealService.saveWithDish(setmealDto);
        return R.success("新增成功");
    }

    /**
     * 分页（条件）查询
     * 1、定参显示；2、查询单表；3、Dto拷贝补充数据Record；4、设置回并回显
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page = {},pageSize = {},name = {}", page, pageSize, name); //{}占位符  //先测试是否有效
        //分页构造器
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);

        //条件查询（name）部分
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Setmeal::getName, name);
        //按更新时间排序
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //执行分页
        setmealService.page(pageInfo, queryWrapper);

        //上面代码写完可以执行一下，会发现category_name套餐分类没有显示（setmeal表里就没有），处理方法与DishFlavorController中大致
        Page<SetmealDto> dtoInfo = new Page<>();
        BeanUtils.copyProperties(pageInfo, dtoInfo, "records"); //忽略考虑records

        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto); //把Setmeal的属性拷贝到SetmealDto

            Category category = categoryService.getById(item.getCategoryId());
            if (category != null) {
                setmealDto.setCategoryName(category.getName());
            }
            return setmealDto;
        }).collect(Collectors.toList());

        dtoInfo.setRecords(list);   //设置回records
        return R.success(dtoInfo);
    }

    /**
     * 根据ID删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping      //↓用于将方法的参数与Web请求的传递的参数进行绑定
    @CacheEvict(value = "setmealCache", allEntries = true)   //表示删除setmealCache所有的数据（keys）
    public R<String> delete(@RequestParam List<Long> ids) {   //Long[] ids也行
        log.info("ids: {}",ids);
        setmealService.removeWithDish(ids);   //涉及两表操作setmeal & setmeal_dish，service层自写操作
        return R.success("套餐删除成功");
    }

    /**
     * （移动端）获取菜品分类对应的套餐  -> 返回套餐
     * 传入参数{...data} -> Setmeal
     * setmealListApi({categoryId:this.categoryId,status:1})前端代码 -> 传入的是Setmeal表中的category_id（参数用Setmeal）
     * 思路：根据category_id查询setmeal，再根据setmeal返回
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache", key = "#setmeal.categoryId + '_' + #setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        //取出
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();      //↓（忽略null判断SQL），前端参数传入的status是1（起售状态）
        //select * from setmeal where category_id = setmeal.category_id and status = setmeal.status order by update_time desc;
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //执行
        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 根据套餐ID获取套餐对应的菜品（发现问题，无菜品图片image属性返回，在SetmealDto加该属性，用Dish表查放入）
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    public R<List<SetmealDto>> getDish(@PathVariable Long id) {
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        //select * from setmeal_dish where setmeal_id = ?;（id;）  因为是点击套餐才有用，故id不为null
        queryWrapper.eq(SetmealDish::getSetmealId,id);

        List<SetmealDish> dishes = setmealDishService.list(queryWrapper);
        List<SetmealDto> list = dishes.stream().map((item) -> {
            SetmealDto dto = new SetmealDto();
            //拷贝
            BeanUtils.copyProperties(item, dto);
            //查询图片 select * from dish where id = dish_id再取出get图片（这时候需补上DishService）
            dto.setImage(dishService.getById(item.getDishId()).getImage());
            return dto;
        }).collect(Collectors.toList());

        return R.success(list);
    }
}
