package com.example.cli.s3.command;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class HelloWorldCommand {

    @ShellMethod(key = "greet")
    public String greet(@ShellOption String name) {
        return "Hello there, " + name + "!";
    }

    @ShellMethod(key = "marco")
    public String marco() {
        return "polo";
    }
}
