package com.CollabSphere.CollabSphere;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.CollabSphere.CollabSphere.Entity")
@EnableJpaRepositories(basePackages = "com.CollabSphere.CollabSphere.Repository")
@ComponentScan(basePackages = "com.CollabSphere.CollabSphere")
public class CollabSphereApplication {

	public static void main(String[] args) {
		SpringApplication.run(CollabSphereApplication.class, args);
	}

}
