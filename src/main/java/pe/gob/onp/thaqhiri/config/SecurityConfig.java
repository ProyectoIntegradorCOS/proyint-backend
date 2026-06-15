package pe.gob.onp.thaqhiri.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import pe.gob.onp.thaqhiri.auth.SaaAuthenticationEntryPoint;
import pe.gob.onp.thaqhiri.auth.SaaAuthenticationFilter;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SaaAuthenticationFilter authenticationFilter,
            SaaAuthenticationEntryPoint entryPoint
    ) throws Exception {
        http
        .cors(cors -> {})
        .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(registry -> registry
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api/health/**",
                                "/api/auth/token",
                                // // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-23 11:18 UTC-5 (Lima)][desc: Permite Actuator para métricas Prometheus][obj: SecurityConfig actuator permit]
                                "/actuator/health/**",
                                "/actuator/info",
                                "/actuator/prometheus"
                        ).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(configurer -> configurer.authenticationEntryPoint(entryPoint))
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
