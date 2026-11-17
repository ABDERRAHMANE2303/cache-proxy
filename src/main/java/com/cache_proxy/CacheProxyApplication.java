package com.cache_proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CacheProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(CacheProxyApplication.class, args);
	}

}
