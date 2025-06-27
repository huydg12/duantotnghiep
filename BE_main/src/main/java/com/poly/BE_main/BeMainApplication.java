package com.poly.BE_main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
		"com.poly.BE_main.controller",
		"com.poly.BE_main.model",
		"com.poly.BE_main.repository",
		"com.poly.BE_main.service",
		"com.poly.BE_main.config"
})
public class BeMainApplication {
	public static void main(String[] args) {
		SpringApplication.run(BeMainApplication.class, args);
	}

}
