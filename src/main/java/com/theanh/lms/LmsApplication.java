package com.theanh.lms;

import com.theanh.lms.config.JwtProperties;
import com.theanh.lms.config.RoleMappingProperties;
import com.theanh.lms.config.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = {"com.theanh.lms", "com.theanh.common"})
@EnableConfigurationProperties({StorageProperties.class, JwtProperties.class, RoleMappingProperties.class})
public class LmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(LmsApplication.class, args);
	}

}
