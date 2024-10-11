/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package com.riversoforion.lena.example;

import java.io.PrintStream;

public abstract class ExampleApplication {

    protected boolean isHelpRequested(String[] args) {

        for (String arg : args) {
            if (arg.equals("-h") || arg.equals("--help")) {
                return true;
            }
        }
        return false;
    }

    protected abstract void printHelp(PrintStream out);
}
