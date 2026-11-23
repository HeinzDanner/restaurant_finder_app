package de.hepaza.demo.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.url}")
    private String appUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF aus, sonst werden POST/PUT/DELETE vom Frontend abgelehnt
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Nur das Anlegen eines Restaurants braucht einen Login
                        .requestMatchers(HttpMethod.POST, "/api/restaurants").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/restaurants/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/restaurants/**").authenticated()
                        // "Wer bin ich?" braucht natürlich auch einen Login
                        .requestMatchers("/api/auth/me").authenticated()
                        // Alles andere ist offen
                        .anyRequest().permitAll())
                // Nicht eingeloggt → 401 statt Weiterleitung (damit axios es erkennt)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                // GitHub-Login, danach zurück zum Frontend
                .oauth2Login(o -> o.defaultSuccessUrl(appUrl, true))
                // Abmelden per POST /api/auth/logout
                .logout(l -> l
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpStatus.OK.value())));
        return http.build();
    }
}
