package ma.projet.events.security;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

/**
 * Service utilitaire pour gérer la sécurité directement depuis les vues Vaadin (UI).
 * Permet de récupérer l'utilisateur courant ou de le déconnecter.
 */
@Component
public class SecurityService {

    private static final String LOGOUT_SUCCESS_URL = "/";

    /**
     * Récupère les détails de l'utilisateur actuellement connecté.
     * @return UserDetails si connecté, null sinon.
     */
    public UserDetails getAuthenticatedUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        // Vérifie si un contexte de sécurité existe
        if (context.getAuthentication() == null) {
            return null;
        }

        Object principal = context.getAuthentication().getPrincipal();
        // Vérifie que le principal est bien un utilisateur standard
        if (principal instanceof UserDetails) {
            return (UserDetails) principal;
        }
        return null;
    }

    /**
     * Gère la déconnexion manuelle depuis un bouton de l'interface Vaadin.
     * Nettoie le contexte de sécurité et redirige vers l'accueil.
     */
    public void logout() {
        UI.getCurrent().getPage().setLocation(LOGOUT_SUCCESS_URL);
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(
                VaadinServletRequest.getCurrent().getHttpServletRequest(),
                null,
                null
        );
    }

    /**
     * Vérifie simplement si l'utilisateur est connecté.
     */
    public boolean isAuthenticated() {
        return getAuthenticatedUser() != null;
    }
}