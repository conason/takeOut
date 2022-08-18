package com.conason.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.conason.reggie.dto.DishDto;
import com.conason.reggie.entity.Dish;

public interface DishService extends IService<Dish> {
    public void saveWithFlavor(DishDto dishDto);

    public DishDto getByIdWithFlavor(Long id);

    public void updateWithFlavor(DishDto dishDto);

    public void updateBatchByIds(Long[] ids, int i);
}
