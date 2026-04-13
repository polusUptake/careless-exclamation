package com.proj1.oops_backend.login;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = "com.proj1.oops_backend")
@RestController
public class OopsBackendApplication {

 public static void main(String[] args) {
      SpringApplication.run(OopsBackendApplication.class, args);
    }
}
