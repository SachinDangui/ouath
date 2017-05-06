package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoRestTemplateFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.util.Map;

@SpringBootApplication
public class OuathEdgeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OuathEdgeServiceApplication.class, args);
    }

    @Bean
    OAuth2RestTemplate restTemplate(UserInfoRestTemplateFactory userInfoRestTemplateFactory) {

        return userInfoRestTemplateFactory.getUserInfoRestTemplate();
    }
}

@Configuration
@EnableOAuth2Sso
class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/**").authorizeRequests()
                .antMatchers("/", "/login**", "/webjars**").permitAll()
                .anyRequest().authenticated()
                .and()
                .logout().logoutSuccessUrl("/").permitAll()
                .and()
                .csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
    }
}

@Configuration
@RestController
@EnableResourceServer
class PrincipalRestController extends ResourceServerConfigurerAdapter {

    @RequestMapping("/user")
    Principal principal(Principal p) {
        return p;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/hi").authorizeRequests().anyRequest().authenticated();
    }
}

@RestController
class GreetingEdgeServiceRestController {

    private final RestTemplate restTemplate;

    @Autowired
    GreetingEdgeServiceRestController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @RequestMapping("/hi")
    Map<String, String> greet() {
        return this.restTemplate.exchange("http://localhost:8080/hi",
                HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, String>>() {
                }).getBody();
    }
}