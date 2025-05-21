package com.vserdiuk.casestudy.service;

import com.vserdiuk.casestudy.dto.CreateOrderDTO;
import com.vserdiuk.casestudy.dto.OrderDTO;

public interface OrderService {

    OrderDTO createOrder(CreateOrderDTO dto);

    void cancelOrder(Long orderId);

    void payOrder(Long orderId);

    void expireOrderById(Long orderId);
}
