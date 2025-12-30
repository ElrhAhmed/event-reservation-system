package ma.projet.events.ui.view.client;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import ma.projet.events.security.SecurityService;
import ma.projet.events.service.UserService;
import ma.projet.events.ui.layout.UserLayout;
import ma.projet.events.ui.view.client.ProfileView;

@Route(value = "profile", layout = UserLayout.class) // Layout CLIENT
@PageTitle("Mon Profil | FESTIVENT")
@RolesAllowed("CLIENT")
public class ClientProfileView extends ProfileView {
    public ClientProfileView(UserService userService, SecurityService securityService) {
        super(userService, securityService);
    }
}