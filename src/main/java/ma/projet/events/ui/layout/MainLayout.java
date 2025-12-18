package ma. projet.events.ui.layout;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin. flow.component.applayout.DrawerToggle;
import com.vaadin.flow. component.avatar.Avatar;
import com.vaadin.flow.component.button. Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon. VaadinIcon;
import com.vaadin.flow.component. orderedlayout.FlexComponent;
import com.vaadin. flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow. component.sidenav.SideNavItem;
import ma.projet.events.entity.Role;
import ma.projet.events.entity.User;

/**
 * Layout principal pour les pages authentifiées
 * Menu adaptatif selon le rôle de l'utilisateur (CLIENT, ORGANIZER, ADMIN)
 */
public class MainLayout extends AppLayout {

    private User currentUser;

    /**
     * Constructeur temporaire avec utilisateur mock
     * TODO Phase 10 : Récupérer l'utilisateur depuis Spring Security
     */
    public MainLayout() {
        // Pour l'instant, utilisateur fictif pour tester
        this.currentUser = createMockUser(Role.CLIENT);

        createHeader();
        createDrawer();
    }

    /**
     * Crée le header avec logo, info utilisateur et déconnexion
     */
    private void createHeader() {
        // Logo avec icône
        Icon logoIcon = VaadinIcon. STAR.create();
        logoIcon.getStyle()
                .set("color", "var(--festivent-accent)")
                .set("margin-right", "var(--lumo-space-xs)");

        H1 logo = new H1("Festivent");
        logo.getStyle()
                .set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0")
                .set("color", "var(--festivent-primary)")
                .set("font-weight", "700");

        HorizontalLayout logoLayout = new HorizontalLayout(logoIcon, logo);
        logoLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        logoLayout.setSpacing(false);

        // Drawer toggle (bouton menu hamburger)
        DrawerToggle toggle = new DrawerToggle();

        // Info utilisateur
        HorizontalLayout userInfo = createUserInfo();

        // Header layout
        HorizontalLayout header = new HorizontalLayout(
                toggle,
                logoLayout,
                userInfo
        );
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.setSpacing(true);
        header.setPadding(true);
        header.getStyle()
                .set("background-color", "var(--festivent-bg-card)")
                .set("box-shadow", "var(--festivent-shadow-md)");

        // Expansion pour pousser userInfo à droite
        header. expand(logoLayout);

        addToNavbar(header);
    }

    /**
     * Crée la section info utilisateur (avatar + nom + déconnexion)
     */
    private HorizontalLayout createUserInfo() {
        // Avatar
        Avatar avatar = new Avatar(currentUser.getNomComplet());
        avatar.setColorIndex(1);

        // Nom + Rôle
        Span nameSpan = new Span(currentUser.getNomComplet());
        nameSpan.getStyle()
                .set("font-weight", "600")
                .set("color", "var(--festivent-text-primary)");

        Span roleSpan = new Span(currentUser.getRole().getLabel());
        roleSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--festivent-text-secondary)");

        VerticalLayout userDetails = new VerticalLayout(nameSpan, roleSpan);
        userDetails.setSpacing(false);
        userDetails.setPadding(false);

        // Bouton déconnexion
        Button logoutButton = new Button("Déconnexion", VaadinIcon.SIGN_OUT.create());
        logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        // TODO Phase 10 : Implémenter la déconnexion

        HorizontalLayout userInfo = new HorizontalLayout(avatar, userDetails, logoutButton);
        userInfo.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        userInfo.setSpacing(true);

        return userInfo;
    }

    /**
     * Crée le menu latéral (Drawer) avec navigation selon le rôle
     */
    private void createDrawer() {
        SideNav nav = new SideNav();
        nav.setCollapsible(true);

        // Menu selon le rôle
        switch (currentUser.getRole()) {
            case CLIENT -> addClientMenu(nav);
            case ORGANIZER -> addOrganizerMenu(nav);
            case ADMIN -> addAdminMenu(nav);
        }

        // Style du drawer
        VerticalLayout drawerContent = new VerticalLayout(nav);
        drawerContent. setSizeFull();
        drawerContent.setPadding(false);
        drawerContent.setSpacing(false);
        drawerContent.getStyle()
                .set("background-color", "var(--festivent-bg)");

        addToDrawer(drawerContent);
    }

    /**
     * Menu CLIENT : Tableau de bord, Mes réservations, Profil
     */
    private void addClientMenu(SideNav nav) {
        nav.addItem(new SideNavItem("Tableau de bord", "dashboard", VaadinIcon. DASHBOARD.create()));
        nav.addItem(new SideNavItem("Mes réservations", "my-reservations", VaadinIcon.TICKET.create()));
        nav.addItem(new SideNavItem("Événements", "events", VaadinIcon.CALENDAR. create()));
        nav.addItem(new SideNavItem("Mon profil", "profile", VaadinIcon.USER.create()));
    }

    /**
     * Menu ORGANIZER : Dashboard organizer, Mes événements, Réservations
     */
    private void addOrganizerMenu(SideNav nav) {
        nav.addItem(new SideNavItem("Tableau de bord", "organizer/dashboard", VaadinIcon. DASHBOARD.create()));
        nav.addItem(new SideNavItem("Mes événements", "organizer/events", VaadinIcon.CALENDAR.create()));
        nav.addItem(new SideNavItem("Réservations", "organizer/reservations", VaadinIcon. TICKET.create()));

        // Section CLIENT (un organisateur est aussi client)
        Span separator = new Span("Espace personnel");
        separator.getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--festivent-text-tertiary)")
                .set("padding", "var(--lumo-space-s)")
                .set("display", "block");

        nav.addItem(new SideNavItem("Mes réservations", "my-reservations", VaadinIcon.TICKET.create()));
        nav.addItem(new SideNavItem("Mon profil", "profile", VaadinIcon.USER.create()));
    }

    /**
     * Menu ADMIN : Dashboard admin, Utilisateurs, Événements, Réservations
     */
    private void addAdminMenu(SideNav nav) {
        nav.addItem(new SideNavItem("Dashboard Admin", "admin/dashboard", VaadinIcon.DASHBOARD.create()));
        nav.addItem(new SideNavItem("Utilisateurs", "admin/users", VaadinIcon. USERS.create()));
        nav.addItem(new SideNavItem("Événements", "admin/events", VaadinIcon. CALENDAR.create()));
        nav.addItem(new SideNavItem("Réservations", "admin/reservations", VaadinIcon.TICKET.create()));

        // Section personnelle
        nav.addItem(new SideNavItem("Mon profil", "profile", VaadinIcon.USER.create()));
    }

    /**
     * Crée un utilisateur fictif pour les tests (Phase 2-9)
     * Sera supprimé en Phase 10 (authentification réelle)
     */
    private User createMockUser(Role role) {
        User user = new User();
        user.setId(1L);
        user.setNom("Test");
        user.setPrenom("Utilisateur");
        user.setEmail("test@festivent.ma");
        user.setRole(role);
        user.setActif(true);
        return user;
    }
}