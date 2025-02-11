package com.nambb.iocdemo.service;

import com.nambb.iocdemo.annotation.Autowired;
import com.nambb.iocdemo.annotation.Component;

@Component
public class OrderService {

    @Autowired
    private PaymentService paymentService;

    public void doOrder() {
        paymentService.doPayment();
    }
}
