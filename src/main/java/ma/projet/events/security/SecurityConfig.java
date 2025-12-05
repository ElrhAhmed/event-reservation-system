package ma.projet.events.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Désactiver la protection CSRF (nécessaire pour la console H2 et les POST simples pour l'instant)
                .csrf(csrf -> csrf.disable())

                // 2. Autoriser les frames (nécessaire pour l'affichage de la console H2)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                // 3. Gestion des autorisations
                .authorizeHttpRequests(auth -> auth
                        // Autoriser l'accès à la console H2 sans mot de passe
                        .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                        // Pour le développement, on autorise tout le reste aussi (on sécurisera à la Phase 5)
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}