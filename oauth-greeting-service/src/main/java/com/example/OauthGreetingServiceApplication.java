package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

@SpringBootApplication
@RestController
@EnableResourceServer
public class OauthGreetingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OauthGreetingServiceApplication.class, args);
    }


    @RequestMapping("/hi")
    Map<String, String> greetings(Principal p) {
        return Collections.singletonMap("content", "Hello " + p.getName());
    }
}
