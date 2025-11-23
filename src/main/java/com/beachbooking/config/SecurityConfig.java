package com.beachbooking.config;

import com.beachbooking.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configurazione principale di Spring Security.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Password encoder BCrypt.
     */
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }

    /**
     * Authentication provider.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
//        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Authentication manager.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Configurazione della security filter chain.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disabilita CSRF (usiamo JWT)
                .csrf(csrf -> csrf.disable())

                // Configura CORS
                .cors(cors -> cors.configure(http))

                // Sessioni stateless (REST API)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configurazione autorizzazioni
                .authorizeHttpRequests(auth -> auth
                        // Endpoint pubblici
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/public/**",
                                "/api/tenants/register",
                                "/api/tenants/check-slug/**",
                                "/health",
                                "/actuator/**"
                        ).permitAll()

                        // Swagger/OpenAPI (se lo aggiungiamo)
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Admin endpoints - solo SUPER_ADMIN
                        .requestMatchers("/api/admin/**").hasRole("SUPER_ADMIN")

                        // Tenant admin endpoints
                        .requestMatchers("/api/tenant-admin/**").hasAnyRole("TENANT_ADMIN", "SUPER_ADMIN")

                        // Staff endpoints
                        .requestMatchers("/api/staff/**").hasAnyRole("STAFF", "TENANT_ADMIN", "SUPER_ADMIN")

                        // Tutti gli altri endpoint richiedono autenticazione
                        .anyRequest().authenticated()
                )

                // Aggiungi il filtro JWT prima del filtro di autenticazione standard
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Configura l'authentication provider
                .authenticationProvider(authenticationProvider());

        return http.build();
    }
}