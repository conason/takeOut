package com.conason.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.conason.reggie.entity.Employee;
import com.conason.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;



    /**
     * 员工登录  没有对用户的账号密码进行trim
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public Request<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        LambdaQueryWrapper<Employee> employeeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        employeeLambdaQueryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(employeeLambdaQueryWrapper);

        if(emp == null){
            return Request.error("登录失败");
        }
        if(!emp.getPassword().equals(password)){
            return Request.error("登录失败");
        }

        if(emp.getStatus() == 0){
            return Request.error("账号禁用中");
        }

        request.getSession().setAttribute("employee",emp.getId());
        return Request.success(emp);
    }


    /**
     * 员工登出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public Request<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return Request.success("退出成功");
    }

    /**
     * 添加员工
     * @param request
     * @param employee
     * @return
     */
    @PostMapping
    public Request<String> save(HttpServletRequest request, @RequestBody Employee employee){
        Long ID = (Long) request.getSession().getAttribute("employee");

        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes(StandardCharsets.UTF_8)));

        /*employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        employee.setCreateUser(ID);
        employee.setUpdateUser(ID);*/

        employeeService.save(employee);

        return Request.success("新增员工成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Request<Page> page(int page, int pageSize, String name){
        //MP分页构造器
        Page pageInfo = new Page(page, pageSize);
        //Lambda条件构造器
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //过滤条件
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //排序条件
        lambdaQueryWrapper.orderByDesc(Employee::getCreateTime);

        employeeService.page(pageInfo,lambdaQueryWrapper);

        return Request.success(pageInfo);
    }

    /**
     * 更新员工信息
     * @param request
     * @param employee
     * @return
     */
    @PutMapping
    public Request<String> update(HttpServletRequest request,@RequestBody Employee employee){
        Long ID = (Long) request.getSession().getAttribute("employee");

        //前端只能处理16位（需要用信息转换器 或者前端将接收的LONG型转为字符串）
        //employee.setUpdateUser(ID);
        //employee.setUpdateTime(LocalDateTime.now());

        employeeService.updateById(employee);

        return Request.success("修改成功");
    }

    /**
     * 根据ID查询员工
     * 配合更新员工信息完成员工信息修改功能
     * 修改页面不应该向后端接受数据回显页面，应由前端自行完成
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Request<Employee> getById(@PathVariable Long id){

        Employee employee = employeeService.getById(id);

        return Request.success(employee);

    }
}
