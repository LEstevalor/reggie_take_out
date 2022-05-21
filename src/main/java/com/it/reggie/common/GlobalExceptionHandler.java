package com.it.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.Controller;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ResourceBundle;

/**
 * 全局异常处理
 */
@ControllerAdvice(annotations = {RestController.class/*, Controller.class*/})  //只要加了RestController、Controller注解，就会被处理器处理
@ResponseBody   //返回成JSON数据
@Slf4j   //日志
public class GlobalExceptionHandler {
    /**
     * 异常处理方法
     * SQLIntegrityConstraintViolationException对于新增账号已存在而重复的情况（Mysql已设置账号唯一）
     * @param ex
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        log.error(ex.getMessage());

        if (ex.getMessage().contains("Duplicate entry")) {   //Duplicate entry 'zhangsan' for key 'employee.idx_username'异常语句
            String[] split = ex.getMessage().split(" ");
            String msg = split[2] + "已存在";   //取出重复的账号
            return R.error(msg);
        }
        return R.error("未知错误");
    }

    /**
     * 异常处理方法 （源于删除分类关联套餐、菜品）
     * @param ex
     * @return
     */
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex) {
        log.error(ex.getMessage());
        return R.error(ex.getMessage());
    }
}
