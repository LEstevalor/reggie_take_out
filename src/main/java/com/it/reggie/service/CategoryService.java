package com.it.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.it.reggie.pojo.Category;

public interface CategoryService extends IService<Category> {
    void remove(Long id);
}
