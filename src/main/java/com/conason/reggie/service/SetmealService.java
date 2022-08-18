package com.conason.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.conason.reggie.dto.SetmealDto;
import com.conason.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    /**
     * 新增套餐
      * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐
     * @param ids
     */
    public void removeWithDish(List<Long> ids);
}
