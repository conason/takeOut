package com.conason.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.conason.reggie.dto.DishDto;
import com.conason.reggie.entity.Category;
import com.conason.reggie.entity.Dish;
import com.conason.reggie.entity.DishFlavor;
import com.conason.reggie.service.CategoryService;
import com.conason.reggie.service.DishFlavorService;
import com.conason.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 分页查询菜品
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public Request<Page> page(int page, int pageSize, String name){
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>(page,pageSize);
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.like(name != null, Dish::getName,name);
        dishLambdaQueryWrapper.orderByDesc(Dish::getSort);
        dishService.page(pageInfo,dishLambdaQueryWrapper);

        //copy pageInfo->dishDtoPage
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        List<Dish> records = pageInfo.getRecords();

        //将菜品对应的分类id查询出分类名
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }
            return dishDto;
        }).collect(Collectors.toList());


        dishDtoPage.setRecords(list);

        return Request.success(dishDtoPage);
    }

    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    public Request<String> delete(Long[] ids){
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId,ids);

        List<Dish> dishes = dishService.list(queryWrapper);

        for (Dish dish : dishes) {
            String key = "dish_" + dish.getCategoryId();
            redisTemplate.delete(key);
        }


        dishService.removeByIds(Arrays.asList(ids));
        return Request.success("删除成功");
    }

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public Request<String> save(@RequestBody DishDto dishDto){

        dishService.saveWithFlavor(dishDto);

        String idCategory = "dish_" + dishDto.getCategoryId();
        redisTemplate.delete(idCategory);

        return Request.success("新增成功");
    }

    /**
     * 更新菜品数据
     * @param dishDto
     * @return
     */
    @PutMapping
    public Request<String> update(@RequestBody DishDto dishDto){

        dishService.updateWithFlavor(dishDto);

        String idCategory = "dish_" + dishDto.getCategoryId();
        redisTemplate.delete(idCategory);

        return Request.success("修改成功");
    }

    /**
     * 根据id获取dishDto
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Request<DishDto> getOne(@PathVariable Long id){
        DishDto byIdWithFlavor = dishService.getByIdWithFlavor(id);
        return Request.success(byIdWithFlavor);
    }

    /**
     * 停售
     * @param ids
     * @return
     */
    @PostMapping("/status/0")
    public Request<String> updateStatic0 (Long[] ids){
        LambdaQueryWrapper<Dish> LambdaQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper.in(Dish::getId,ids);
        List<Dish> dishes = dishService.list(LambdaQueryWrapper);
        for (Dish dish : dishes) {
            String key = "dish_" + dish.getCategoryId();
            redisTemplate.delete(key);
        }

        dishService.updateBatchByIds(ids,0);

        return Request.success("停售成功");

    }

    /**
     * 启售
     * @param ids
     * @return
     */
    @PostMapping("/status/1")
    public Request<String> updateStatic1 (Long[] ids){
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(Dish::getId,ids);
        List<Dish> dishes = dishService.list(dishLambdaQueryWrapper);
        for (Dish dish : dishes) {
            String key = "dish_" + dish.getCategoryId();
            redisTemplate.delete(key);
        }

        dishService.updateBatchByIds(ids,1);

        return Request.success("启售成功");
    }


    @GetMapping("/list")
    public Request<List<DishDto>> list(Dish dish){
        //拼接菜品分类id
        String idCategory = "dish_" + dish.getCategoryId();

        List<DishDto> dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(idCategory);

        if (dishDtoList != null) {
            return Request.success(dishDtoList);
        }


        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //按照菜品分类
        dishLambdaQueryWrapper.eq(dish.getCategoryId()!= null, Dish::getCategoryId, dish.getCategoryId());
        //菜品分类启售状态
        dishLambdaQueryWrapper.eq(Dish::getStatus,1);
        dishLambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        //查询菜品分类下的菜品/套餐
        List<Dish> list = dishService.list(dishLambdaQueryWrapper);

        //将dish转成dishDto
        dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);

            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }

            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId, item.getId());
            List<DishFlavor> flavors = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(flavors);

            return dishDto;

        }).collect(Collectors.toList());

        redisTemplate.opsForValue().set(idCategory,dishDtoList,60, TimeUnit.MINUTES);

        return Request.success(dishDtoList);
    }
}
