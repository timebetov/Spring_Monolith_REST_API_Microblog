package com.github.timebetov.microblog.configs;

import com.github.timebetov.microblog.exceptionHandlers.AccessDenyHandler;
import com.github.timebetov.microblog.exceptionHandlers.AuthEntryPointHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class MicroblogSecurityConfig {

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {

        http    // CSRF -> Cross Site Request Forgery
                .csrf(AbstractHttpConfigurer::disable); // Disabled CSRF protection

        http    // Routing
                .authorizeHttpRequests((req) -> req
                        .requestMatchers("/auth/login", "/users/create", "/error").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/users/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                );

        http    // Authentication
                .formLogin(AbstractHttpConfigurer::disable)     // Disabled Form Login Authentication
                .httpBasic(hbc -> hbc.authenticationEntryPoint(
                        new AuthEntryPointHandler()
                ));

        http    // Exception Handling Globally
                .exceptionHandling(ehc -> {
                    ehc.accessDeniedHandler(new AccessDenyHandler());
                    ehc.authenticationEntryPoint(new AuthEntryPointHandler());
                });
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {

        MicroblogUsernamePwdAuthProvider authenticationProvider = new MicroblogUsernamePwdAuthProvider(userDetailsService, passwordEncoder);
        ProviderManager providerManager = new ProviderManager(authenticationProvider);
        providerManager.setEraseCredentialsAfterAuthentication(false);
        return providerManager;
    }
}
