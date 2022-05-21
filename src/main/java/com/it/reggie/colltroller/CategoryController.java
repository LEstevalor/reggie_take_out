package com.it.reggie.colltroller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.reggie.common.R;
import com.it.reggie.pojo.Category;
import com.it.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 分类管理
 */
@Slf4j   //日志
@RestController  //@Controller + @ResponseBody（异步请求，用于controller返回对象，返回json/xml...数据）
@RequestMapping("/category") //springMVC控制器上方，注释MVC路径作用
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category) {
        log.info("分类信息:{}",category.toString());
        categoryService.save(category);
        return R.success("新增成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize) {
        log.info("page = {},pageSize = {}", page, pageSize);  //{}占位符
        //1 构造分页构造器
        Page<Category> pageInfo = new Page<>(page,pageSize);
        // 条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件，根据sort进行排序
        queryWrapper.orderByAsc(Category::getSort);
        //2 执行查询
        categoryService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 根据ID删除分类
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids) {
        log.info("删除分类：id为{}", ids);
        //categoryService.removeById(ids);    //会发现直接删除，比如增一个再删是无效的（显示成功但删不去），因为未检查是否关联
        categoryService.remove(ids);
        return R.success("分类信息删除成功");
    }

    /**
     * 编辑方法
     * @param category
     * @return
     */
    @PutMapping           //↓页面返回的是JSON的形式，用@RequestBody
    public R<String> update(@RequestBody Category category) {
        log.info("修改分类信息：{}", category);
        categoryService.updateById(category);
        return R.success("修改成功");
    }

    /**
     * 根据添加查询分类数据
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category) {
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        //SELECT id,type,name,sort,create_time,update_time,create_user,update_user FROM category WHERE (type = ?) ORDER BY sort ASC,update_time DESC
        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }

}
