package com.conason.reggie.controller;

import com.conason.reggie.common.BaseContext;
import com.conason.reggie.entity.Orders;
import com.conason.reggie.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/order")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;

    @PostMapping("/submit")
    public Request<String> order(@RequestBody Orders order){


        ordersService.orderById(order);

        return Request.success("支付成功");
    }
}
