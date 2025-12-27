package ma.projet.events.ui. layout;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component. button.Button;
import com.vaadin. flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin. flow.component.orderedlayout.FlexComponent;
import com.vaadin. flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component. orderedlayout. Scroller;
import com.vaadin.flow.component.orderedlayout. VerticalLayout;
import com.vaadin.flow.router. RouterLink;
import com. vaadin.flow. theme.lumo.LumoUtility;
import ma.projet.events. entity.Role;
import ma. projet.events.entity.User;
import ma.projet.events.security.SecurityService;

/**
 * Layout principal pour les utilisateurs connectés.
 * Sidebar sombre avec navigation + zone de contenu.
 */
public class MainLayout extends AppLayout {

    private final SecurityService securityService;
    private final User currentUser;

    public MainLayout(SecurityService securityService) {
        this. securityService = securityService;
        this.currentUser = securityService.getAuthenticatedUser();

        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    /**
     * Header minimaliste (juste le toggle pour mobile)
     */
    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");
        toggle.getStyle().set("color", "var(--festivent-secondary-text)");

        HorizontalLayout header = new HorizontalLayout(toggle);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment. CENTER);
        header.setPadding(true);
        header.getStyle()
                .set("background-color", "white")
                .set("padding", "var(--festivent-space-sm) var(--festivent-space-md)");

        addToNavbar(true, header);
    }

    /**
     * Contenu du drawer (sidebar sombre)
     */
    private void addDrawerContent() {
        VerticalLayout drawerLayout = new VerticalLayout();
        drawerLayout.setSizeFull();
        drawerLayout. setPadding(false);
        drawerLayout.setSpacing(false);
        drawerLayout. getStyle()
                .set("background-color", "#1e293b")
                .set("color", "white");

        // === LOGO SECTION ===
        HorizontalLayout logoSection = createLogoSection();

        // === NAVIGATION ===
        VerticalLayout navigation = createNavigation();
        Scroller scroller = new Scroller(navigation);
        scroller.setClassName(LumoUtility. Padding.SMALL);

        // === USER SECTION (bas du sidebar) ===
        VerticalLayout userSection = createUserSection();

        // Assemblage
        drawerLayout.add(logoSection, scroller, userSection);
        drawerLayout.setFlexGrow(1, scroller);

        addToDrawer(drawerLayout);
    }

    /**
     * Section logo en haut du sidebar
     */
    private HorizontalLayout createLogoSection() {
        Icon calendarIcon = VaadinIcon.CALENDAR.create();
        calendarIcon.getStyle()
                .set("color", "var(--festivent-primary)")
                .set("width", "24px")
                .set("height", "24px");

        Span brandName = new Span("EventReserve");
        brandName.getStyle()
                .set("font-size", "var(--lumo-font-size-l)")
                .set("font-weight", "700")
                .set("color", "white");

        HorizontalLayout logoSection = new HorizontalLayout(calendarIcon, brandName);
        logoSection.setAlignItems(FlexComponent.Alignment. CENTER);
        logoSection.setSpacing(true);
        logoSection.setWidthFull();
        logoSection.getStyle()
                .set("gap", "var(--festivent-space-sm)")
                .set("padding", "var(--festivent-space-lg)")
                .set("cursor", "pointer");

        logoSection.addClickListener(e -> UI.getCurrent().navigate(""));

        return logoSection;
    }

    /**
     * Navigation selon le rôle
     */
    private VerticalLayout createNavigation() {
        VerticalLayout nav = new VerticalLayout();
        nav.setPadding(false);
        nav.setSpacing(false);
        nav.setWidthFull();
        nav.getStyle().set("gap", "4px");

        // === MENU CLIENT (tous les rôles) ===
        nav.add(createNavItem("Dashboard", VaadinIcon. DASHBOARD, getClientDashboardRoute()));
        nav.add(createNavItem("My Reservations", VaadinIcon.TICKET, "client/reservations"));
        nav.add(createNavItem("Profile", VaadinIcon.USER, "client/profile"));

        // === MENU ORGANISATEUR ===
        if (currentUser.getRole() == Role.ORGANIZER || currentUser.getRole() == Role.ADMIN) {
            nav.add(createSectionDivider());
            nav.add(createSectionLabel("ORGANIZER"));
            nav.add(createNavItem("My Events", VaadinIcon.CALENDAR, "organizer/events"));
            nav.add(createNavItem("Create Event", VaadinIcon.PLUS_CIRCLE, "organizer/event/new"));
        }

        // === MENU ADMIN ===
        if (currentUser.getRole() == Role.ADMIN) {
            nav.add(createSectionDivider());
            nav.add(createSectionLabel("ADMINISTRATION"));
            nav.add(createNavItem("Users", VaadinIcon. USERS, "admin/users"));
            nav.add(createNavItem("All Events", VaadinIcon.CALENDAR_O, "admin/events"));
            nav.add(createNavItem("All Reservations", VaadinIcon.LIST, "admin/reservations"));
        }

        return nav;
    }

    /**
     * Crée un item de navigation
     */
    private HorizontalLayout createNavItem(String label, VaadinIcon iconType, String route) {
        Icon icon = iconType. create();
        icon.setSize("20px");
        icon.getStyle().set("color", "#94a3b8");

        Span text = new Span(label);
        text.getStyle()
                .set("color", "#e2e8f0")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("font-weight", "500");

        HorizontalLayout item = new HorizontalLayout(icon, text);
        item.setAlignItems(FlexComponent.Alignment. CENTER);
        item.setSpacing(true);
        item.setWidthFull();
        item.getStyle()
                .set("gap", "var(--festivent-space-sm)")
                .set("padding", "var(--festivent-space-sm) var(--festivent-space-md)")
                .set("margin", "0 var(--festivent-space-sm)")
                .set("border-radius", "var(--festivent-radius-md)")
                .set("cursor", "pointer")
                .set("transition", "all 0.15s ease");

        // Hover effect
        item. getElement().addEventListener("mouseenter", e -> {
            item.getStyle().set("background-color", "#334155");
            icon.getStyle().set("color", "var(--festivent-primary)");
            text.getStyle().set("color", "white");
        });
        item.getElement().addEventListener("mouseleave", e -> {
            item.getStyle().set("background-color", "transparent");
            icon.getStyle().set("color", "#94a3b8");
            text.getStyle().set("color", "#e2e8f0");
        });

        // Navigation
        item.addClickListener(e -> UI.getCurrent().navigate(route));

        return item;
    }

    /**
     * Séparateur de section
     */
    private Hr createSectionDivider() {
        Hr divider = new Hr();
        divider.getStyle()
                .set("border-color", "#334155")
                .set("margin", "var(--festivent-space-md) var(--festivent-space-md)");
        return divider;
    }

    /**
     * Label de section
     */
    private Span createSectionLabel(String label) {
        Span sectionLabel = new Span(label);
        sectionLabel.getStyle()
                .set("color", "#64748b")
                .set("font-size", "var(--lumo-font-size-xxs)")
                .set("font-weight", "700")
                .set("letter-spacing", "0.05em")
                .set("padding", "var(--festivent-space-xs) var(--festivent-space-lg)")
                .set("text-transform", "uppercase");
        return sectionLabel;
    }

    /**
     * Section utilisateur en bas du sidebar
     */
    private VerticalLayout createUserSection() {
        VerticalLayout userSection = new VerticalLayout();
        userSection.setPadding(true);
        userSection.setSpacing(false);
        userSection.setWidthFull();
        userSection.getStyle()
                .set("border-top", "1px solid #334155")
                .set("margin-top", "auto")
                .set("gap", "var(--festivent-space-sm)");

        // User info
        HorizontalLayout userInfo = new HorizontalLayout();
        userInfo. setAlignItems(FlexComponent.Alignment. CENTER);
        userInfo.setSpacing(true);
        userInfo.setWidthFull();
        userInfo.getStyle().set("gap", "var(--festivent-space-sm)");

        Icon userIcon = VaadinIcon.USER. create();
        userIcon.setSize("20px");
        userIcon. getStyle().set("color", "#94a3b8");

        VerticalLayout userDetails = new VerticalLayout();
        userDetails.setPadding(false);
        userDetails.setSpacing(false);

        Span userName = new Span(currentUser.getPrenom() + " " + currentUser.getNom());
        userName.getStyle()
                .set("color", "white")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("font-weight", "600");

        Span userRole = new Span(currentUser.getRole().getLabel());
        userRole.getStyle()
                .set("color", "#64748b")
                .set("font-size", "var(--lumo-font-size-xs)");

        userDetails.add(userName, userRole);
        userInfo.add(userIcon, userDetails);

        // Logout button
        Button logoutButton = new Button("Logout", VaadinIcon. SIGN_OUT. create());
        logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        logoutButton.setWidthFull();
        logoutButton. getStyle()
                .set("color", "#ef4444")
                .set("justify-content", "flex-start")
                .set("padding-left", "var(--festivent-space-xs)");

        logoutButton.addClickListener(e -> {
            securityService.logout();
            UI.getCurrent().navigate("login");
            UI.getCurrent().getPage().reload();
        });

        userSection.add(userInfo, logoutButton);
        return userSection;
    }

    /**
     * Route du dashboard selon le rôle
     */
    private String getClientDashboardRoute() {
        return switch (currentUser.getRole()) {
            case ADMIN -> "admin/dashboard";
            case ORGANIZER -> "organizer/dashboard";
            case CLIENT -> "client/dashboard";
        };
    }
}