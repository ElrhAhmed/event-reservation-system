package ma.projet.events.ui.navigation;

import com.vaadin.flow.component.UI;
import ma.projet.events.entity.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class NavigationManager {

    /* =========================
       AUTHENTIFICATION
       ========================= */

    private Authentication getAuth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public boolean isAuthenticated() {
        Authentication auth = getAuth();
        return auth != null
                && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal());
    }

    public boolean hasRole(Role role) {
        Authentication auth = getAuth();
        if (auth == null) return false;

        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role.name()));
    }

    /* =========================
       NAVIGATION SIMPLE
       ========================= */

    public void goToHome() {
        UI.getCurrent().navigate("");
    }

    public void goToLogin() {
        UI.getCurrent().navigate("login");
    }

    public void goToRegister() {
        UI.getCurrent().navigate("register");
    }

    public void goToEvents() {
        UI.getCurrent().navigate("events");
    }

    public void goToEventDetail(Long eventId) {
        UI.getCurrent().navigate("event/" + eventId);
    }

    public void goToReservation(Long eventId) {
        UI.getCurrent().navigate("event/" + eventId + "/reserve");
    }
    public void goToClientReservations() {
        UI.getCurrent().navigate("my-reservations");
    }

    public void goToEditEvent(Long eventId) {
        UI.getCurrent().navigate("organizer/event/edit/" + eventId);
    }

    public void goToProfile() {
        if (hasRole(Role.ADMIN)) {
            UI.getCurrent().navigate("admin/profile");
        } else if (hasRole(Role.ORGANIZER)) {
            UI.getCurrent().navigate("organizer/profile");
        } else {
            UI.getCurrent().navigate("profile");
        }
    }

    public void goToMyEvents() {
        UI.getCurrent().navigate("organizer/events");
    }

    public void goToCreateEvent() {
        UI.getCurrent().navigate("organizer/event/new");
    }

    public void goToOrganizerReservations() {
        UI.getCurrent().navigate("organizer/reservations");
    }

    public void goToAdminUsers() {
        UI.getCurrent().navigate("admin/users");
    }

    public void goToAdminEvents() {
        UI.getCurrent().navigate("admin/events");
    }

    public void goToAdminReservations() {
        UI.getCurrent().navigate("admin/reservations");
    }

    /* =========================
       REDIRECTION POST-LOGIN
       ========================= */

    public void redirectAfterLogin() {
        if (!isAuthenticated()) {
            goToHome();
            return;
        }

        if (hasRole(Role.ADMIN)) {
            goToAdminDashboard();
        } else if (hasRole(Role.ORGANIZER)) {
            goToOrganizerDashboard();
        } else {
            goToClientDashboard();
        }
    }

    /* =========================
       DASHBOARDS
       ========================= */

    public void goToClientDashboard() {
        UI.getCurrent().navigate("dashboard");
    }

    public void goToOrganizerDashboard() {
        UI.getCurrent().navigate("organizer/dashboard");
    }

    public void goToAdminDashboard() {
        UI.getCurrent().navigate("admin/dashboard");
    }

    public void goToEventReservations(Long eventId) {
        UI.getCurrent().navigate("organizer/event/" + eventId + "/reservations");
    }
}
