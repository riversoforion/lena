/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package com.riversoforion.lena.example;

import java.io.PrintStream;

public class SimpleConfigExample extends ExampleApplication {

    private final ApplicationConfig config = new ApplicationConfig();

    public static void main(String[] args) {

        System.out.println("=== Simple Configuration Example ===");
        SimpleConfigExample example = new SimpleConfigExample();

        if (example.isHelpRequested(args)) {
            example.printHelp(System.out);
            return;
        }

        example.printConfig(System.out);
    }

    public void printConfig(PrintStream out) {

        out.printf("%25s = %s%n", "serviceUrl", config.serviceUrl());
        out.printf("%25s = %s%n", "serviceApiKey", config.serviceApiKey());
        out.printf("%25s = %s%n", "serviceApiSecret", config.serviceApiSecret());
        out.printf("%25s = %d%n", "netConnectionTimeout", config.netConnectionTimeout());
        out.printf("%25s = %d%n", "netReadTimeout", config.netReadTimeout());
        out.printf("%25s = %b%n", "isLocalMode", config.isLocalMode());
    }

    @Override
    protected void printHelp(PrintStream out) {

        out.print("""
                  Demonstrates a simple usage of ConfigurationProperties
                  
                  Run with environment variables or system properties set. The following configuration properties are supported:
                      Environment Variable    System Property         Type
                      SERVICE_URL             service.url             string
                      SERVICE_API_KEY         service.api.key         string
                      SERVICE_API_SECRET      service.api.secret      string
                      NET_CONNECTION_TIMEOUT  net.connection.timeout  long
                      NET_READ_TIMEOUT        net.read.timeout        long
                      LOCAL_MODE              local.mode              flag
                  """);
    }
}
