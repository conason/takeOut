package com.conason.reggie.dto;

import com.conason.reggie.entity.Setmeal;
import com.conason.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
