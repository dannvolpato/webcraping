package br.com.gredom.webscraping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableFeignClients
@SpringBootApplication
public class WebscrapingApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebscrapingApplication.class, args);
	}
}