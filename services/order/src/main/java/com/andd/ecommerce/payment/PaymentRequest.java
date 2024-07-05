package com.andd.ecommerce.payment;

import com.andd.ecommerce.dto.CustomerResponse;
import com.andd.ecommerce.order.PaymentMethod;

import java.math.BigDecimal;

public record PaymentRequest(
        BigDecimal amount,
        PaymentMethod paymentMethod,
        Integer orderId,
        String orderReference,
        CustomerResponse customer
) {
}
