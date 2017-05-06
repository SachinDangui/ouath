package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@SpringBootApplication
@EnableResourceServer
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}

@RestController
class PrincipalRestController{

    @RequestMapping("/user")
    Principal principal(Principal p){
        return p;
    }
}

@Configuration
@EnableAuthorizationServer
class OauthServiceConfigurator extends AuthorizationServerConfigurerAdapter {

    private final AuthenticationManager authenticationManager;

    @Autowired
    OauthServiceConfigurator(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }


    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(this.authenticationManager);
        
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient("android")
                .authorizedGrantTypes("authorization_code", "password", "implicit")
                .scopes("read", "write", "openid")
                .secret("secret")
                .and()
                .withClient("html5")
                .authorizedGrantTypes("authorization_code", "password", "implicit")
                .scopes("read", "write", "openid")
                .secret("secret");

        super.configure(clients);
    }
}


@Component
class SampleDataCLR implements CommandLineRunner {

    final AccountRepository accountRepository;

    public SampleDataCLR(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void run(String... strings) throws Exception {
        Stream.of("user1,pass1", "user2,pass2", "user3,pass3", "user4,pass4")
                .map(x -> x.split(",")).forEach(tuple -> accountRepository.save(new Account(tuple[0], tuple[1], true)));
    }
}

@Service
class AccountUserDetailService implements UserDetailsService {

    private AccountRepository accountRepository;

    @Autowired
    public AccountUserDetailService(AccountRepository acountRepository) {
        this.accountRepository = acountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        return this.accountRepository.findByUserName(userName)
                .map(account -> new User(account.getUserName(), account.getPassword(),
                        account.isActive(), account.isActive(), account.isActive(), account.isActive(),
                        AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_USER")
                ))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userName));

    }
}

interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUserName(String userName);
}


@Entity
class Account {

    public Account(String userName, String password, boolean active) {
        this.userName = userName;
        this.password = password;
        this.active = active;
    }

    public Account() {
    }

    @Id
    @GeneratedValue

    private Long id;

    private String userName;
    private String password;
    private boolean active;

    public Long getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public boolean isActive() {
        return active;
    }


}
