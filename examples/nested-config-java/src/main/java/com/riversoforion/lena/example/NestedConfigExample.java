/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package com.riversoforion.lena.example;

import java.io.PrintStream;

public class NestedConfigExample extends ExampleApplication {

    private final ApplicationConfig config = new ApplicationConfig();

    public void printConfig(PrintStream out) {
        out.printf("%30s = %s%n", "service.url", config.getService().getUrl());
        out.printf("%30s = %s%n", "service.apiKey", config.getService().getApiKey());
        out.printf("%30s = %s%n", "service.apiSecret", config.getService().getApiSecret());
        out.printf("%30s = %s%n", "service.net.readTimeout", config.getService().getNet().getReadTimeout());
        out.printf("%30s = %s%n", "service.net.connectionTimeout", config.getService().getNet().getConnectionTimeout());
        out.printf("%30s = %s%n", "net.readTimeout", config.getNet().getReadTimeout());
        out.printf("%30s = %s%n", "net.connectionTimeout", config.getNet().getConnectionTimeout());
        out.printf("%30s = %s%n", "localMode", config.isLocalMode());
    }

    @Override
    public void printHelp(PrintStream out) {
        out.print("""
            Demonstrates usage of nested ConfigurationProperties, composed via Java
            sub-classes and explicit instantiation.

            Run with environment variables or system properties set. The top-level `net` and the
            `service`-nested `net` are independent configuration properties and must each be
            supplied separately if you want to override their defaults. The following
            configuration properties are supported:
                Environment Variable            System Property                  Type    Default
                SERVICE_URL                     service.url                      string  (required)
                SERVICE_API_KEY                 service.api.key                  string  (required)
                SERVICE_API_SECRET              service.api.secret               string  (required)
                SERVICE_NET_READ_TIMEOUT        service.net.read.timeout         long    5000
                SERVICE_NET_CONNECTION_TIMEOUT  service.net.connection.timeout   long    5000
                NET_READ_TIMEOUT                net.read.timeout                 long    5000
                NET_CONNECTION_TIMEOUT          net.connection.timeout           long    5000
                LOCAL_MODE                      local.mode                       flag    (required)
            """);
    }

    public static void main(String[] args) {
        System.out.println("=== Nested Configuration Example (Java) ===");
        NestedConfigExample example = new NestedConfigExample();
        if (example.isHelpRequested(args)) {
            example.printHelp(System.out);
            return;
        }
        example.printConfig(System.out);
    }
}
