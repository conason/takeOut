package com.conason.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.conason.reggie.dto.DishDto;
import com.conason.reggie.entity.DishFlavor;
import com.conason.reggie.mapper.DishFlavorMapper;
import com.conason.reggie.service.DishFlavorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {

}
