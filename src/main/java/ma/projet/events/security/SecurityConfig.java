package ma.projet.events.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import ma.projet.events.ui.view.publicview.LoginView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * Configuration principale de la sécurité.
 * Étend VaadinWebSecurity pour faciliter l'intégration avec Vaadin.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends VaadinWebSecurity {

    // Utilisation de BCrypt pour hasher les mots de passe de manière sécurisée
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // 1. Définition des URLs publiques (accessibles sans connexion)
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        new AntPathRequestMatcher("/"),          // Page d'accueil
                        new AntPathRequestMatcher("/login"),     // Page de connexion
                        new AntPathRequestMatcher("/register"),  // Page d'inscription
                        new AntPathRequestMatcher("/events/**"), // Liste des événements
                        new AntPathRequestMatcher("/event/**"),  // Détails d'un événement
                        new AntPathRequestMatcher("/images/**"), // Ressources statiques
                        new AntPathRequestMatcher("/icons/**"),
                        new AntPathRequestMatcher("/h2-console/**"), // Base de données H2 (dev)
                        new AntPathRequestMatcher("/VAADIN/**")      // Ressources internes Vaadin
                ).permitAll()
        );

        // Appel de la configuration parente pour sécuriser les vues Vaadin restantes
        super.configure(http);

        // 2. Définition de la vue de connexion personnalisée (Vaadin)
        setLoginView(http, LoginView.class);

        // 3. Configuration de la déconnexion
        http.logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                .logoutSuccessUrl("/") // Retour à l'accueil après déconnexion
                .permitAll()
        );

        // 4. Gestionnaire de succès de connexion (Redirection par Rôle)
        // C'est ici qu'on décide où envoyer l'utilisateur après son login.
        http.formLogin(form -> form
                .successHandler(new AuthenticationSuccessHandler() {
                    @Override
                    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                        // Récupération des rôles de l'utilisateur connecté
                        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
                        String targetUrl = "/"; // URL par défaut

                        // Redirection conditionnelle
                        if (roles.contains("ROLE_ADMIN")) {
                            targetUrl = "/admin/dashboard";
                        } else if (roles.contains("ROLE_ORGANIZER")) {
                            targetUrl = "/organizer/dashboard";
                        } else if (roles.contains("ROLE_CLIENT")) {
                            targetUrl = "/dashboard";
                        }

                        // Effectuer la redirection
                        response.sendRedirect(targetUrl);
                    }
                })
        );

        // Configuration spécifique pour autoriser la console H2
        http.csrf(csrf -> csrf
                .ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**"))
        );
        http.headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
        );
    }
}