package com.it.reggie.colltroller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.reggie.common.R;
import com.it.reggie.dto.DishDto;
import com.it.reggie.pojo.Category;
import com.it.reggie.pojo.Dish;
import com.it.reggie.pojo.DishFlavor;
import com.it.reggie.service.CategoryService;
import com.it.reggie.service.DishFlavorService;
import com.it.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品管理
 * page.food
 */
@RestController  //ResponseBody异步请求 + Controller层
@Slf4j   //日志
@RequestMapping("/dish")  //路径 - 前端addDish
public class DishFlavorController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto); //为添加到两张表，自己写方法，重点在flavor部分

        //清理所有菜品的缓存数据（对应的套餐可能也涉及到该菜品，故最保险的情况是删除所有）
        Set keys = redisTemplate.keys("dish_*");    //取出所有以“dish_”开头的key
        redisTemplate.delete(keys);                         //del keys

        //清除某个分类
        //String key = "dish_" + dishDto.getCategoryId() + "_1";
        //redisTemplate.delete(key);

        return R.success("新增菜品成功");
    }

    /**
     * 分页（条件）查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")   //↓Get到URL直接获取参数，不需加注解
    public R<Page> page(int page, int pageSize, String name) {
        //构造分页构造器
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishPageInfo = new Page<>();

        //构造条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件（条件查询——对应name参数的作用）
        queryWrapper.like(name != null, Dish::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(pageInfo, queryWrapper);   //不能直接把Dish换成DishDto的原因就是没有DishDto对应的service

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishPageInfo,"records");  //忽略records拷贝

        List<Dish> records = pageInfo.getRecords();

        List<DishDto> list = records.stream().map((item) -> {   //lambda表达式
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId();  //分类ID
            //根据ID查询分类对象（这时候需补上属性categoryService）
            Category category = categoryService.getById(categoryId);
            //注意分类可能为空，所以加个判断
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

        dishPageInfo.setRecords(list);   //设置records，上面其实也可以for，但是lambda和stream读快一些
        return R.success(dishPageInfo);
    }

    /**
     * 根据ID查询菜品信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Dish> getById(@PathVariable Long id) {
        log.info("根据菜品ID查询菜品信息...");

        DishDto dishDto = dishService.getByIdWithFlavor(id);  //自写到DishServiceImpl的方法

        return R.success(dishDto);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping         //↓JSON数据提交
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());

        //类比添加，我们显然需要对DishDto取出信息分别编辑到dish表和dish_flavor表 ——> 去service写
        dishService.updateWithFlavor(dishDto);

        //清理所有菜品的缓存数据（对应的套餐可能也涉及到该菜品，故最保险的情况是删除所有）
        Set keys = redisTemplate.keys("dish_*");    //取出所有以“dish_”开头的key
        redisTemplate.delete(keys);                         //del keys

        return R.success("修改成功");
    }


    /*//查询菜品分类ID（TYPE=1）对应的菜品
    @GetMapping("/list")
    public R<List<Dish>> list(Long categoryId) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getCategoryId,categoryId);
        queryWrapper.eq(Dish::getStatus, 1);
        List<Dish> dishes = dishService.list(queryWrapper);
        return R.success(dishes);
    }*/
    // ↓ 通用性更好，Dish，不仅限于一个categoryId去查询

    /**
     * 查询菜品分类ID（TYPE=1）对应的菜品
     * （改进——移动端查询菜品点开时能选择口味，这时候返回参数Dish -> DishDto）
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        List<DishDto> list = null;

        //动态构造key（重点）
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        //1、redis中获取缓存数据（每个分类对应几种，对应多个分类有多个key）
        list = (List<DishDto>) redisTemplate.opsForValue().get(key);   //get key

        if (list != null) {
            //2、存在则直接返回
            return R.success(list);
        }
        //3、不存在则需查询数据库，并且把数据放入缓存

        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        //添加添加，查询状态为1（起售）
        queryWrapper.eq(Dish::getStatus, 1);
        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> dishes = dishService.list(queryWrapper);

        // Dish -> DishDto （将前面page方法复制过来改进）（但是注意我们这里需要查询口味数据）
        list = dishes.stream().map((item) -> {   //lambda表达式
            DishDto dishDto = new DishDto();
            //Dish的信息拷贝到DishDto
            BeanUtils.copyProperties(item,dishDto);
            //Category属性补充（dish的category_id查询 -> Category主键ID）
            Category category = categoryService.getById(item.getCategoryId());
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            //flavor属性补充（口味）（dish_id查询 -> 口味信息(非主键)）
            LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
            // select * from dish_flavor where dish_id = item.id;
            wrapper.eq(DishFlavor::getDishId, item.getId());
            dishDto.setFlavors(dishFlavorService.list(wrapper));

            return dishDto;
        }).collect(Collectors.toList());

        redisTemplate.opsForValue().set(key, list, 60, TimeUnit.MINUTES);  //setex key 3600 list

        return R.success(list);
    }
}
