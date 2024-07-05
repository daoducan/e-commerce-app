package com.andd.ecommerce.kafka;

import com.andd.ecommerce.dto.CustomerResponse;
import com.andd.ecommerce.dto.PurchaseResponse;
import com.andd.ecommerce.order.PaymentMethod;

import java.math.BigDecimal;
import java.util.List;

public record OrderConfirmation(
        String orderReference,
        BigDecimal totalAmount,
        PaymentMethod paymentMethod,
        CustomerResponse customer,
        List<PurchaseResponse> products
) {
}
