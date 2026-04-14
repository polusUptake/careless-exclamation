package com.proj1.oops_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.proj1.oops_backend.repository")
public class OopsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(OopsBackendApplication.class, args);
    }
}
