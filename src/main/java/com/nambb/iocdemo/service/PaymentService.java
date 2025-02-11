package com.nambb.iocdemo.service;

import com.nambb.iocdemo.annotation.Component;

@Component
public class PaymentService {

    void doPayment() {
        System.out.println("Payment method");
    }
}
