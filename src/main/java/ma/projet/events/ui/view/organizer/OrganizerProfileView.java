package ma.projet.events.ui.view.organizer;

import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import ma.projet.events.security.SecurityService;
import ma.projet.events.service.UserService;
import ma.projet.events.ui.layout.OrganizerLayout; // Layout ORGANIZER
import ma.projet.events.ui.view.client.ProfileView;

@Route(value = "organizer/profile", layout = OrganizerLayout.class)
@RolesAllowed({"ORGANIZER", "ADMIN"})
public class OrganizerProfileView extends ProfileView {
    public OrganizerProfileView(UserService userService, SecurityService securityService) {
        super(userService, securityService);
    }
}