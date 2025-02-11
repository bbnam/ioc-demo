package com.nambb.iocdemo;


import com.nambb.iocdemo.annotation.Autowired;
import com.nambb.iocdemo.annotation.Component;
import com.nambb.iocdemo.loader.ContextLoader;
import com.nambb.iocdemo.loader.Runner;
import com.nambb.iocdemo.service.OrderService;

@Component
public class IocDemoApplication implements Runner {

    @Autowired
    private OrderService orderService;

    public static void main(String[] args) {
        ContextLoader.getInstance().load("com.nambb.iocdemo");
    }

    @Override
    public void run() {
        System.out.println("Application ready to start");
        orderService.doOrder();
    }
}
