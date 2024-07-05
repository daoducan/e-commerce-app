package com.andd.ecommerce.service;

import com.andd.ecommerce.customer.CustomerClient;
import com.andd.ecommerce.dto.OrderLineRequest;
import com.andd.ecommerce.dto.OrderRequest;
import com.andd.ecommerce.dto.OrderResponse;
import com.andd.ecommerce.dto.PurchaseRequest;
import com.andd.ecommerce.exception.BusinessException;
import com.andd.ecommerce.kafka.OrderConfirmation;
import com.andd.ecommerce.kafka.OrderProducer;
import com.andd.ecommerce.mapper.OrderMapper;
import com.andd.ecommerce.orderline.OrderLineService;
import com.andd.ecommerce.payment.PaymentClient;
import com.andd.ecommerce.payment.PaymentRequest;
import com.andd.ecommerce.product.ProductClient;
import com.andd.ecommerce.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerClient customerClient;
    private final ProductClient productClient;
    private final OrderMapper orderMapper;
    private final OrderLineService orderLineService;
    private final OrderProducer orderProducer;
    private final PaymentClient paymentClient;

    public Integer createOrder(OrderRequest orderRequest) {
        //Check customer --> OpenFeign (use Feign to call API)
        var customer = this.customerClient.findCustomerById(orderRequest.customerId())
                .orElseThrow(() -> new BusinessException("Cannot create order:: No customer exists with the provided ID" + orderRequest.customerId()));

        //purchase the products --> product-microservice (use RestTemplate to call API)
        var purchasedProducts = this.productClient.purchaseProducts(orderRequest.products());
        var order = this.orderRepository.save(orderMapper.toOrder(orderRequest));

        //persit order object
        for (PurchaseRequest purchaseRequest : orderRequest.products()) {
            orderLineService.saveOrderLine(
                    new OrderLineRequest(
                            null,
                            order.getId(),
                            purchaseRequest.productId(),
                            purchaseRequest.quantity()
                    )
            );
        }

        //persit the order line

        //start payment process
        var paymentRequest = new PaymentRequest(
                orderRequest.amount(),
                orderRequest.paymentMethod(),
                order.getId(),
                order.getReference(),
                customer
        );
        paymentClient.requestOrderPayment(paymentRequest);

        //send the order confirmation --> notification-service (kafka broker)
        orderProducer.sendOrderConfirmation(
                new OrderConfirmation(
                        orderRequest.reference(),
                        orderRequest.amount(),
                        orderRequest.paymentMethod(),
                        customer,
                        purchasedProducts
                )
        );
        return order.getId();
    }

    public List<OrderResponse> findAll() {
        return orderRepository.findAll()
                .stream()
                .map(orderMapper::fromOrder)
                .collect(Collectors.toList());
    }

    public OrderResponse findById(Integer orderId) {
        return orderRepository.findById(orderId)
                .map(orderMapper::fromOrder)
                .orElseThrow(() -> new EntityNotFoundException(String.format("No order found with provided ID: %d", orderId)));
    }
}
