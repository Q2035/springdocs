package com.test.chapter01.pojo;

public class DefaultServiceLocator {
    private static ClientService clientService = new ClientService.ClientServiceImpl();

    public ClientService createClientServiceInstance() {
        System.out.println("createClientServiceInstance invoked");
        return clientService;
    }
}