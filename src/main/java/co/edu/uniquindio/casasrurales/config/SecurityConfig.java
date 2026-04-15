package co.edu.uniquindio.casasrurales.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configura las reglas de seguridad de la API REST.
 * Define qué rutas son públicas, qué roles pueden acceder
 * y el codificador de contraseñas usado por Spring Security.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())  // Desabililar CSRF para APIs REST
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas
                        .requestMatchers("/auth/registro/**").permitAll()
                        .requestMatchers("/auth/login/**").permitAll()
                        .requestMatchers("/auth/me").authenticated()
                        // Búsqueda: solo clientes autenticados
                        .requestMatchers("/api/busqueda/**").hasRole("CLIENTE")
                        // Cualquier otra solicitud requiere autenticación
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> basic.disable());  // Desabilitar HTTP Basic Auth para APIs

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:3001", "http://localhost:3002"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
