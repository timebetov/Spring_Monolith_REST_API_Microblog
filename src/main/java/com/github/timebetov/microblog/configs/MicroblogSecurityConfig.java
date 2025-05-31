package com.github.timebetov.microblog.configs;

import com.github.timebetov.microblog.handlers.AccessDenyHandler;
import com.github.timebetov.microblog.handlers.AuthEntryPointHandler;
import com.github.timebetov.microblog.filters.JwtTokenGeneratorFilter;
import com.github.timebetov.microblog.filters.JwtTokenValidatorFilter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@SecurityScheme(
        name = "basicAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "basic"                // Basic Auth
)
public class MicroblogSecurityConfig {

    private final JwtTokenGeneratorFilter jwtGeneratorFilter;
    private final JwtTokenValidatorFilter jwtValidatorFilter;

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {

        http    // Session Related Configurations
                .sessionManagement(smc -> smc.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http    // CSRF -> Cross Site Request Forgery
                .csrf(AbstractHttpConfigurer::disable); // Disabled CSRF protection

        http    // Routing
                .authorizeHttpRequests((req) -> req
                        .requestMatchers("/auth/logout").authenticated()
                        .requestMatchers(
                                "/auth/**",
                                "/error",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**").permitAll()
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

        http    // Filters
                .addFilterBefore(jwtValidatorFilter, BasicAuthenticationFilter.class)
                .addFilterAfter(jwtGeneratorFilter, BasicAuthenticationFilter.class);

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
