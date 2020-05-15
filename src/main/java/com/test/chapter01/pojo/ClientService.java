package com.test.chapter01.pojo;

public class ClientService {

    private static ClientService clientService = new ClientService();

    private ClientService() {
        System.out.println("ClientService invoked");
    }

    public static ClientService createInstance() {
        return clientService;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    static class ClientServiceImpl extends ClientService {

    }
}