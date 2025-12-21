package ma.projet.events.security;

import com.vaadin.flow.server. VaadinServletRequest;
import ma.projet.events.entity.User;
import ma.projet.events.repository.UserRepository;
import org.springframework.security. core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework. security.core.userdetails. UsernameNotFoundException;
import org. springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service utilitaire pour gérer l'authentification et l'utilisateur connecté
 */
@Service
public class SecurityService {

    private final UserRepository userRepository;

    public SecurityService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Récupère l'utilisateur actuellement connecté (entité User complète)
     */
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || ! authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("Aucun utilisateur connecté");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Utilisateur introuvable :  " + email
                ));
    }

    /**
     * Récupère l'utilisateur connecté de manière optionnelle
     */
    public Optional<User> getAuthenticatedUserOptional() {
        try {
            return Optional.of(getAuthenticatedUser());
        } catch (UsernameNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Récupère l'ID de l'utilisateur connecté
     */
    public Long getAuthenticatedUserId() {
        return getAuthenticatedUser().getId();
    }

    /**
     * Récupère l'email de l'utilisateur connecté
     */
    public Optional<String> getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return Optional. of(authentication.getName());
        }
        return Optional.empty();
    }

    /**
     * Vérifie si un utilisateur est connecté
     */
    public boolean isUserLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName());
    }

    /**
     * Vérifie si l'utilisateur connecté est un ADMIN
     */
    public boolean isAdmin() {
        return getAuthenticatedUserOptional()
                .map(User::isAdmin)
                .orElse(false);
    }

    /**
     * Vérifie si l'utilisateur connecté est un ORGANIZER
     */
    public boolean isOrganizer() {
        return getAuthenticatedUserOptional()
                .map(User::isOrganizer)
                .orElse(false);
    }

    /**
     * Déconnecte l'utilisateur actuel
     * ✅ Invalide la session Spring Security ET la session HTTP
     */
    public void logout() {
        // 1. Récupérer l'authentification actuelle
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. Effacer le contexte de sécurité
        SecurityContextHolder.clearContext();

        // 3. Invalider la session HTTP (Vaadin)
        VaadinServletRequest request = VaadinServletRequest.getCurrent();
        if (request != null && authentication != null) {
            SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
            logoutHandler.logout(
                    request.getHttpServletRequest(),
                    null,
                    authentication
            );
        }
    }
}