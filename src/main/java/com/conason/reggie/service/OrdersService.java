package com.conason.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.conason.reggie.entity.Orders;

public interface OrdersService extends IService<Orders> {
    /**
     * 根据用户ID进行订单结算
     * @param order
     */
    public void orderById(Orders order);
}
