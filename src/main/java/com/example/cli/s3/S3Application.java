package com.example.cli.s3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.command.annotation.CommandScan;

@CommandScan
@SpringBootApplication
public class S3Application {

	public static void main(String[] args) {
		SpringApplication.run(S3Application.class, args);
	}

}
