package com.it.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.reggie.mapper.EmployeeMapper;
import com.it.reggie.pojo.Employee;
import com.it.reggie.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service   //service层对应注解                          ↓对应dao接口    ↓对应实体             ↓对应service接口
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}
