package com.example.cli.s3;

import com.example.cli.s3.properties.S3BucketProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(S3BucketProperties.class)
public class S3Application {

	public static void main(String[] args) {
		SpringApplication.run(S3Application.class, args);
	}

}
