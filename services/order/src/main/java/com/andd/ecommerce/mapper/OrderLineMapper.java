package com.andd.ecommerce.mapper;

import com.andd.ecommerce.dto.OrderLineRequest;
import com.andd.ecommerce.dto.OrderLineResponse;
import com.andd.ecommerce.order.Order;
import com.andd.ecommerce.orderline.OrderLine;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderLineMapper {
    public OrderLine toOrderLine(OrderLineRequest orderLineRequest) {
        return OrderLine.builder()
                .id(orderLineRequest.id())
                .quality(orderLineRequest.quantity())
                .order(Order.builder()
                        .id(orderLineRequest.orderId())
                        .build())
                .productId(orderLineRequest.productId())
                .build();
    }

    public OrderLineResponse toOrderLineResponse(OrderLine orderLine) {
        return new OrderLineResponse(
                orderLine.getId(),
                orderLine.getQuality()
        );
    }
}
