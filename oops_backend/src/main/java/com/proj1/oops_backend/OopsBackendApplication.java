package com.proj1.oops_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class OopsBackendApplication {

 public static void main(String[] args) {
      SpringApplication.run(OopsBackendApplication.class, args);
    }
}
