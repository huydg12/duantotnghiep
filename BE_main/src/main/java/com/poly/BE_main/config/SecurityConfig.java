package com.poly.BE_main.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origin:http://localhost:5173}")
    private String allowedOrigin;

    @Autowired
    private JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors() // <-- Cho phép CORS
                .and()
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/product/**", "/productDetail/**", "/brand/**",
                                "/category/**", "/color/**", "/size/**", "/sole/**", "/collar/**",
                                "/promotion/**", "/image/**").permitAll()
                        .requestMatchers("/account/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/employee/showInformationEmployee/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/employee/updateInformation/**").authenticated()
                        .requestMatchers("/employee/**").hasRole("ADMIN")
                        .requestMatchers("/inventory/**", "/importReceipt/**", "/importReceiptDetail/**",
                                "/statistic/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(HttpMethod.POST, "/bill/create").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/bill/*/cancel").authenticated()
                        .requestMatchers(HttpMethod.GET, "/bill/invoicecustomer/**", "/bill/{billId}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/bill/*/status-history").authenticated()
                        .requestMatchers("/bill/**", "/billDetail/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(HttpMethod.POST, "/product/**", "/productDetail/**", "/brand/**",
                                "/category/**", "/color/**", "/size/**", "/sole/**", "/collar/**",
                                "/promotion/**", "/image/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(HttpMethod.PUT, "/product/**", "/productDetail/**", "/brand/**",
                                "/category/**", "/color/**", "/size/**", "/sole/**", "/collar/**",
                                "/promotion/**", "/image/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(HttpMethod.DELETE, "/product/**", "/productDetail/**", "/brand/**",
                                "/category/**", "/color/**", "/size/**", "/sole/**", "/collar/**",
                                "/promotion/**", "/image/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Cho phép truy cập từ http://localhost:5173
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigin));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // Nếu cần gửi cookie, JWT...

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
