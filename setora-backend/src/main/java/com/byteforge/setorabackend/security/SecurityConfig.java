package com.byteforge.setorabackend.security;

import com.byteforge.setorabackend.auth.DatabaseUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static void writeSecurityError(HttpServletResponse response, int status, String code, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write("{\"code\":\"" + code + "\",\"message\":\"" + message + "\",\"fieldErrors\":{}}");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        AuthenticationEntryPoint unauthorizedHandler = (request, response, exception) ->
                writeSecurityError(response, 401, "UNAUTHORIZED", "Authentication required");

        http.csrf(AbstractHttpConfigurer::disable) // turn this back on if we switch to cookies
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(
                        SessionCreationPolicy
                                .STATELESS // this means spring won't store authentication in an http session
                        ))
                .authorizeHttpRequests(auth -> auth
                        // swagger documentation
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/error")
                        .permitAll()

                        // public authentication endpoints
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/auth/refresh")
                        .permitAll()

                        // user management(admin only)
                        .requestMatchers("/api/users", "/api/users/**")
                        .hasRole("ADMIN")

                        // all other endpoints require login
                        .anyRequest()
                        .authenticated())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(
                        oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                                .authenticationEntryPoint(unauthorizedHandler)
                                .accessDeniedHandler((request, response, exception) ->
                                        writeSecurityError(response, 403, "FORBIDDEN", "Access denied")))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler)
                        .accessDeniedHandler((request, response, exception) ->
                                writeSecurityError(response, 403, "FORBIDDEN", "Access denied")));

        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins((List.of("http://localhost:4200")));
        configuration.setAllowedMethods((List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")));
        configuration.setAllowedHeaders((List.of("Authorization", "Content-Type")));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            DatabaseUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();

        authorities.setAuthoritiesClaimName("role");
        authorities.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }
}
