package ma.projet.events.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration des Beans de sécurité
 * Définit le PasswordEncoder utilisé dans toute l'application
 */
@Configuration
public class SecurityBeansConfig {

    /**
     * Bean PasswordEncoder utilisant BCrypt
     * BCrypt est l'algorithme recommandé pour hasher les mots de passe
     *
     * Ce Bean sera automatiquement injecté dans UserService et autres classes
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}