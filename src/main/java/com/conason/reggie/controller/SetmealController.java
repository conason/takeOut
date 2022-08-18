package com.conason.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.conason.reggie.dto.SetmealDto;
import com.conason.reggie.entity.Category;
import com.conason.reggie.entity.Setmeal;
import com.conason.reggie.service.CategoryService;
import com.conason.reggie.service.SetmealDishService;
import com.conason.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 分页
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Request<Page> page(int page, int pageSize, String name){
        Page<Setmeal> setmealPage = new Page<>(page,pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>(page,pageSize);
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.like(name != null, Setmeal::getName, name);
        setmealLambdaQueryWrapper.orderByDesc(Setmeal::getCreateTime);
        setmealService.page(setmealPage,setmealLambdaQueryWrapper);

        //copy setmealPage->setmealDtoPage
        BeanUtils.copyProperties(setmealPage,setmealDtoPage,"records");

        //根据套餐id取出套餐分类名
        List<Setmeal> records = setmealPage.getRecords();
        List<SetmealDto> setmealDtoList = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);

            Category category = categoryService.getById(item.getCategoryId());
            setmealDto.setCategoryName(category.getName());

            return setmealDto;
        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(setmealDtoList);

        return Request.success(setmealDtoPage);
    }

    /**
     * 保存新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public Request<String> save(@RequestBody SetmealDto setmealDto){

        setmealService.saveWithDish(setmealDto);

        return Request.success("保存成功");
    }

    @DeleteMapping
    public Request<String> deleteSetmeal(@RequestParam List<Long> ids){

        setmealService.removeWithDish(ids);

        return Request.success("删除成功");
    }

    @PostMapping("/status/{status}")
    public Request<String> updateStatus(@PathVariable int status,@RequestParam List<Long> ids){

        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.in(Setmeal::getId,ids);

        List<Setmeal> setmeals = setmealService.list(setmealLambdaQueryWrapper);
        setmeals = setmeals.stream().map((item) -> {
            item.setStatus(status);
            return item;
        }).collect(Collectors.toList());

        setmealService.updateBatchById(setmeals);

        String tips = status == 1? "启售成功": "停售成功";
        return Request.success(tips);
    }

    @GetMapping("/list")
    public Request<List<Setmeal>> getBySetmeal(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        lambdaQueryWrapper.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        lambdaQueryWrapper.orderByDesc(Setmeal::getCreateTime);

        List<Setmeal> setmealist = setmealService.list(lambdaQueryWrapper);

        return Request.success(setmealist);

    }
}
