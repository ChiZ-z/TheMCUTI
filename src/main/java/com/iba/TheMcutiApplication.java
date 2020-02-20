package com.iba;

import com.iba.config.OAuth2ClientConfig;
import com.iba.config.OAuth2Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({OAuth2Config.class, OAuth2ClientConfig.class})
public class TheMcutiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TheMcutiApplication.class, args);
	}
}
