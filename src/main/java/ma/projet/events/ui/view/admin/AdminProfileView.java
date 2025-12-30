package ma.projet.events.ui.view.admin;

import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import ma.projet.events.security.SecurityService;
import ma.projet.events.service.UserService;
import ma.projet.events.ui.layout.AdminLayout; // Layout ADMIN
import ma.projet.events.ui.view.client.ProfileView;

@Route(value = "admin/profile", layout = AdminLayout.class)
@RolesAllowed("ADMIN")
public class AdminProfileView extends ProfileView {
    public AdminProfileView(UserService userService, SecurityService securityService) {
        super(userService, securityService);
    }
}