package com.conason.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.conason.reggie.common.BaseContext;
import com.conason.reggie.entity.ShoppingCart;
import com.conason.reggie.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 用户添加的菜品/套餐 增加数量
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public Request<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){

        Long id = BaseContext.getCurrentId();
        shoppingCart.setUserId(id);

        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId,id);
        shoppingCartLambdaQueryWrapper.eq(dishId != null,ShoppingCart::getDishId,dishId);
        shoppingCartLambdaQueryWrapper.eq(dishId == null,ShoppingCart::getSetmealId,shoppingCart.getSetmealId());

        ShoppingCart one = shoppingCartService.getOne(shoppingCartLambdaQueryWrapper);

        if (one != null) {
            one.setNumber(one.getNumber() + 1);
        }else {
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            one = shoppingCart;
        }

        shoppingCartService.saveOrUpdate(one);

        return Request.success(one);
    }

    /**
     * 将购物车对应的菜品/套餐数量减一 为零删除
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public Request<String> sub(@RequestBody ShoppingCart shoppingCart){
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);
        queryWrapper.eq(shoppingCart.getDishId() != null,ShoppingCart::getDishId,shoppingCart.getDishId());
        queryWrapper.eq(shoppingCart.getDishId() == null,ShoppingCart::getSetmealId,shoppingCart.getSetmealId());


        ShoppingCart one = shoppingCartService.getOne(queryWrapper);

        if (one.getNumber() == 1) {
            shoppingCartService.remove(queryWrapper);
        }else {
            one.setNumber(one.getNumber() - 1);
            shoppingCartService.updateById(one);
        }

        return Request.success("操作成功");
    }




    /**
     * 根据用户ID返回用户购物车信息
     * @return
     */
    @GetMapping("/list")
    public Request<List<ShoppingCart>> list(){
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,currentId);
        lambdaQueryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> shoppingCarts = shoppingCartService.list(lambdaQueryWrapper);

        return Request.success(shoppingCarts);
    }

    @DeleteMapping("/clean")
    public Request<String> deleteCart(){
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);

        shoppingCartService.remove(queryWrapper);
        return Request.success("清空购物车");
    }

}
