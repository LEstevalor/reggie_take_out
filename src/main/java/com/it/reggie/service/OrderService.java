package com.it.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.it.reggie.pojo.Orders;

public interface OrderService extends IService<Orders> {
    /**
     * 提交订单-下单
     * @param orders
     */
    void submit(Orders orders);
}
