package com.it.reggie.colltroller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.reggie.common.R;
import com.it.reggie.pojo.Employee;
import com.it.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j   //日志
@RestController  //@Controller + @ResponseBody（异步请求，用于controller返回对象，返回json/xml...数据）
@RequestMapping("/employee") //springMVC控制器上方，注释MVC路径作用
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")   //Post是因为前端以POST的形式
                           // ↓需要把员工对应的ID存到session内表示登录成功，想获取当前用户的话，直接request获取
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {

        //1、将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        //emp↓数据库查过来的对象，可以用getOne是因为用户名是唯一的
        Employee emp = employeeService.getOne(queryWrapper);

        //3、如果没有查询到则返回登录失败结果
        if (emp == null) {
            return R.error("登录失败");
        }

        //4、密码比对，“这果不一致则返回登录失败结果
        if (!emp.getPassword().equals(password)) {
            return R.error("登录失败");
        }

        //5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if (emp.getStatus() == 0) {
            return R.error("账号已禁用");
        }

        //6、登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee", emp.getId());  //存入Session的是Id

        return R.success(emp);
    }

    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        //清除Session中保存的当前登录员工的ID
        request.getSession().removeAttribute("employee");  //登录获取的什么就对应删除的什么，employee对应上面登录方法的命名
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * @param request
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee) {
        log.info("员工信息:{}",employee.toString());

        //设置初始密码123456，需MD5加密处理
        //employee.setPassword("123456");
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        //↓，改用公共字段，代码改为 -> MyMetaObjecthandler类中（Employee填充了TableField）
        /*employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //获得当前登录用户的ID
        Long empId = (Long) request.getSession().getAttribute("employee");

        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);*/

        employeeService.save(employee);
        //数据库中给了states设置了默认值1，所以不需设置

        return R.success("新增成功");
    }

    /**
     * 员工信息分页查询
     * @param page
     * @param pageSize
     * @param name    //是为了对应后续搜索时的分页
     * @return
     */
    @GetMapping("/page")                          //↓是为了对应后续搜索时的分页
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page = {},pageSize = {},name = {}", page, pageSize, name); //{}占位符  //先测试是否有效

        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);

        //构造条件构造器（因为还有可能传过来name，即name!=null的情况）
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //添加一个过滤条件     //↓import org.apache.commons.lang.StringUtils;idea默认会导成util的
        // isNotEmpty相当于判断是否有名字
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //添加排序
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * （起因：修改禁用启用，前端传回一个修改status的原本employee对象，传回字段，需直接修改保存到数据库）
     * 根据ID修改员工信息
     * @param employee
     * @return
     */                    //↓页面返回的是JSON的形式，用@RequestBody
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        // ↓改用公共字段
        /*Long empId = (Long) request.getSession().getAttribute("employee");
        employee.setUpdateTime(LocalDateTime.now());   //修改更新时间
        employee.setUpdateUser(empId);*/

        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }

    /**
     * （起源：编辑页面回写数据，前端传回URL的id来查询数据，而前端代码的 this.ruleForm = res.data就能达到回显的目的）
     * 根据ID查询信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")    //与前面json传过来的数据不同，这里是以URL的形式传的数据
    public R<Employee> getById(@PathVariable Long id) {
        log.info("根据id查询员工信息。。。");
        Employee employee = employeeService.getById(id);
        if (employee != null) {
            return R.success(employee);
        }
        return R.error("没有查询到对应的员工信息");
    }
}
