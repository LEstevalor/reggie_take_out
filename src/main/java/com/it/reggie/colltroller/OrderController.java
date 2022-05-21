package com.it.reggie.colltroller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.reggie.common.BaseContext;
import com.it.reggie.common.R;
import com.it.reggie.dto.OrdersDto;
import com.it.reggie.pojo.OrderDetail;
import com.it.reggie.pojo.Orders;
import com.it.reggie.service.OrderDetailService;
import com.it.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController   //ResponseBody + Controller
@Slf4j  //日志
@RequestMapping("/order")   //URL
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 提交订单-下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> save(@RequestBody Orders orders) {
        log.info("orders:{}",orders);  //测试会发现参数只传来用户地址address与支付方法=1，其他的需要我们用各种service去填充信息
        orderService.submit(orders);   //service去写，调用多个表补充信息
        return R.success("提交成功");
    }

    /**
     * 查看订单 -> 订单分页查询
     * @param page       参数只有页面信息，order_detail表中无用户ID
     * @param pageSize   根据当前用户ID -> order_id -> order_detail
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> page(int page, int pageSize) {

        //分页构造器
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        Page<OrdersDto> pageO = new Page<>();

        //查到order订单 ： SQL select * from orders where user_id = ? order by orderTime DESC
        LambdaQueryWrapper<Orders> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.eq(Orders::getUserId, BaseContext.getCurrentId());
        wrapper1.orderByDesc(Orders::getOrderTime);
        List<Orders> orders = orderService.list(wrapper1);

        //执行分页
        orderService.page(pageInfo,wrapper1);
        BeanUtils.copyProperties(pageInfo, pageO, "records");

        List<OrdersDto> list = orders.stream().map((item) -> {
            OrdersDto dto = new OrdersDto();

            BeanUtils.copyProperties(orders,dto);
            //查到订单详情: SQL select * from order_detail where order_id = ?
            LambdaQueryWrapper<OrderDetail> wrapper2 = new LambdaQueryWrapper<>();
            wrapper2.eq(OrderDetail::getOrderId, item.getId());
            dto.setOrderDetails(orderDetailService.list(wrapper2));

            return dto;
        }).collect(Collectors.toList());

        pageO.setRecords(list);  //设回records
        return R.success(pageO);
    }
}

