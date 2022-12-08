package br.com.gredom.webscraping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.time.ZoneOffset;
import java.util.TimeZone;

@EnableFeignClients
@SpringBootApplication
public class WebscrapingApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
        SpringApplication.run(WebscrapingApplication.class, args);
    }
}