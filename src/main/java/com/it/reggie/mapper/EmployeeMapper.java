package com.it.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.it.reggie.pojo.Employee;
import org.apache.ibatis.annotations.Mapper;

@Mapper   //接口注解
public interface EmployeeMapper extends BaseMapper<Employee> {
}
