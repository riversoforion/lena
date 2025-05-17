/*
 * Copyright (c) 2025. Eric McIntyre / Rivers of Orion
 */
package com.riversoforion.lena.example;

import java.io.PrintStream;

public class AnnotationConfigExample extends ExampleApplication {

    private SimplestConfig simplest;

    public static void main(String[] args) {

        System.out.println("=== Annotation Configuration Example ===");
        AnnotationConfigExample example = new AnnotationConfigExample();

        if (example.isHelpRequested(args)) {
            example.printHelp(System.out);
            return;
        }

        example.printSimplestConfig(System.out);
    }

    public void printSimplestConfig(PrintStream out) {

        out.printf("%25s = %s%n", "serviceUrl", simplest.serviceUrl());
        out.printf("%25s = %s%n", "serviceApiKey", simplest.serviceApiKey());
        out.printf("%25s = %s%n", "serviceApiSecret", simplest.serviceApiSecret());
    }

    @Override
    protected void printHelp(PrintStream out) {

    }
}
