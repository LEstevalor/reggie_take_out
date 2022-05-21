package com.it.reggie.dto;

import com.it.reggie.pojo.OrderDetail;
import com.it.reggie.pojo.Orders;
import lombok.Data;
import java.util.List;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;
	
}
