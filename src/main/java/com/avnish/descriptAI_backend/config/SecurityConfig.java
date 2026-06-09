package com.avnish.descriptAI_backend.config;

import com.avnish.descriptAI_backend.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author Avnish
 * @date 07-06-2026
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf( csrf -> csrf.disable())
                .sessionManagement((session) -> session
                        .sessionCreationPolicy((SessionCreationPolicy.STATELESS) ))
                .authorizeHttpRequests(auth -> auth
                                // 1. Explicitly allow POST for ALL auth endpoints
                                .requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()

                                // 2. Admin restricted POSTs
                                .requestMatchers(HttpMethod.POST, "/api/products/save", "/api/AI/generate").hasRole("ADMIN")

                                // 3. User + Admin GETs
                                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()

                                // 4. Other permitAll (like swagger UI)
                                .requestMatchers("/swagger-ui/**", "/error/**", "/v3/api-docs/**", "/webjars/**","/actuator/health**").permitAll()

                                .anyRequest().authenticated()
                        )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
            DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
            provider.setUserDetailsService(userDetailsService);
            provider.setPasswordEncoder(passwordEncoder());
            return provider;
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
