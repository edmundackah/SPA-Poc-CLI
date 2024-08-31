package com.example.cli.s3;

import com.example.cli.s3.command.DeployCommand;
import com.example.cli.s3.command.MaintenanceCommand;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.command.annotation.CommandScan;
import org.springframework.shell.command.annotation.EnableCommand;

@CommandScan
@SpringBootApplication
public class S3Application {

	public static void main(String[] args) {
		SpringApplication.run(S3Application.class, args);
	}

}
