package ma.projet.events.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;


/**
 * Configuration principale de Spring Security
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Configuration des autorisations
                .authorizeHttpRequests(auth -> auth
                        // ✅ VAADIN - Ressources internes (TRÈS IMPORTANT)
                        .requestMatchers(
                                "/VAADIN/**",
                                "/vaadinServlet/**",
                                "/frontend/**",
                                "/frontend-es5/**",
                                "/frontend-es6/**"
                        ).permitAll()

                        // ✅ Routes publiques
                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/h2-console/**",
                                "/images/**",
                                "/styles/**",
                                "/icons/**",
                                "/favicon.ico"
                        ).permitAll()

                        // Routes protégées
                        .anyRequest().authenticated()
                )

                // Configuration du formulaire de login
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )

                // Configuration de la déconnexion
                .logout(logout -> logout
                        .logoutSuccessUrl("/login")
                        .permitAll()
                )

                // Désactiver CSRF pour H2 console et Vaadin (développement uniquement)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**")
                        .ignoringRequestMatchers("/**") // ⚠️ À RETIRER EN PRODUCTION
                )

                // Autoriser les frames pour H2 console
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions. sameOrigin())
                );

        return http.build();
    }
}