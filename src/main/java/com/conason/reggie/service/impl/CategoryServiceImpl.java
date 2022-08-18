package com.conason.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.conason.reggie.common.CustomException;
import com.conason.reggie.entity.Category;
import com.conason.reggie.entity.Dish;
import com.conason.reggie.entity.Setmeal;
import com.conason.reggie.mapper.CategoryMapper;
import com.conason.reggie.service.CategoryService;
import com.conason.reggie.service.DishService;
import com.conason.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 删除菜品分类，该分类下没有菜品，并且无套餐，方能删除
     * @param id
     */
    @Override
    public void remove(Long id){

        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        int count1 = dishService.count(dishLambdaQueryWrapper);
        if(count1 > 0){
            throw  new CustomException("当前分类有菜品，无法删除");
        }

        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);
        if(count2 > 0){
            throw  new CustomException("当前分类有套餐，无法删除");
        }

        super.removeById(id);
    }

}
