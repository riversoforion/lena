/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package com.riversoforion.lena.example;

import java.io.PrintStream;

public class NestedConfigExample extends ExampleApplication {

    private final ApplicationConfig config = new ApplicationConfig();

    public static void main(String[] args) {

        System.out.println("=== Nested Configuration Example ===");
        NestedConfigExample example = new NestedConfigExample();

        if (example.isHelpRequested(args)) {
            example.printHelp(System.out);
            return;
        }
        example.printConfig(System.out);
    }

    public void printConfig(PrintStream out) {

        //@formatter:off
        out.printf("%30s = %s%n", "service.url", config.service().url());
        out.printf("%30s = %s%n", "service.apiKey", config.service().apiKey());
        out.printf("%30s = %s%n", "service.apiSecret", config.service().apiSecret());
        out.printf("%30s = %s%n", "service.net.readTimeout", config.service().net().readTimeout());
        out.printf("%30s = %s%n", "service.net.connectTimeout", config.service().net().connectTimeout());
        out.printf("%30s = %s%n", "net.readTimeout", config.net().readTimeout());
        out.printf("%30s = %s%n", "net.connectTimeout", config.net().connectTimeout());
        out.printf("%30s = %s%n", "localMode", config.isLocalMode());
        //@formatter:on
    }

    @Override
    protected void printHelp(PrintStream out) {

        out.print("""
                  Demonstrates usage of nested ConfigurationProperties
                  """);
    }
}
